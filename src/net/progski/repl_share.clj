(ns net.progski.repl-share
  (:import [net.progski.repl_share BroadcastReader BroadcastWriter])
  (:use [net.progski.repl-share.broadcast]))

;; Watcher implementation
(def buf (byte-array 1000))

(def *watching* (atom false))

(defn watch*
  "Watch a particular share.  For each message received call f,
  passing it the message."
  [share f]
  (reset! *watching* true)
  (with-open [sock (join-group *group-addr*)]
    (while *watching*
           (.receive sock (make-packet buf))
           (let [msg (deserialize buf)]
             (when (= share (msg :share))
               (if (msg :kill)
                 (reset! *watching* false)
                 (f msg)))))))

(def main-ns (atom *ns*))

(defn watch
  "Watch the share."
  [share]
  (printf "Starting watcher REPL for share %s%n" share)
  (.start
   (Thread. (fn []
              (watch* share
                      #(do (print (str "\r" (apply str (repeat 100 " ")) "\r" (:content %)))
                           (printf "[watching %s] %s=> " share (ns-name @main-ns))
                           (flush))))))
  (let [old-in-ns in-ns]
    (binding [in-ns #(reset! main-ns (old-in-ns %))]
      (clojure.main/repl
       :init #(in-ns (ns-name *ns*))
       :prompt (fn [] (printf "[watching %s] %s=> " share (ns-name *ns*)))))))
             
;; Share implementation
(def content (ref ""))

(defn share
  "Share your REPL with the passed share name."
  [share]
  (binding [*out* (BroadcastWriter. share content *out*)
            *in* (BroadcastReader. share content *in*)]
    (clojure.main/repl
     :prompt (fn [] (printf "[%s] %s=> " share (ns-name *ns*)))
     :flush (fn []
              (broadcast share @content)
              (dosync (ref-set content ""))
              (flush)))))