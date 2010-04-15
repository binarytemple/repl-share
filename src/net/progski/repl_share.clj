(ns net.progski.repl-share
  (:import [net.progski.repl_share InInterceptor OutInterceptor
            ErrInterceptor])
  (:use [net.progski.repl-share.broadcast]
        [clojure.contrib.seq-utils :only (partition-all)]))

;; Watcher implementation
(def *max-size* 914)

;; Incoming raw buffer is max content size plus overhead for other
;; properties.
;; share name: 25 characters
(def buf (byte-array (+ *max-size* 110)))

(def *watching* (atom false))
(def msg-buff (atom []))

(defn build-msg [msgs]
  (reduce str (map #(:content %) (sort #(compare (:order %1) (:order %2)) msgs))))

(defn watch*
  "Watch a particular share.  For each message received call f,
  passing it the message."
  [share f]
  (reset! *watching* true)
  (with-open [sock (join-group *group-addr*)]
    (while @*watching*
           (.receive sock (make-packet buf))
           (if @*watching*
             (let [msg (deserialize buf)]
               (when (= share (msg :share))
                 (swap! msg-buff conj msg)
                 (when (= (:total msg) (count @msg-buff))
                   (f (build-msg @msg-buff))
                   (reset! msg-buff []))))))))

(def main-ns (atom *ns*))

(defn watch
  "Watch the share."
  [share]
  (printf "Starting watcher REPL for share %s%n" share)
  (.start
   (Thread. (fn []
              (watch* share
                      #(do (print (str "\r" (apply str (repeat 100 " ")) "\r" %))
                           (printf "[watching %s] %s=> " share (ns-name @main-ns))
                           (flush))))))
  (let [old-in-ns in-ns]
    (binding [in-ns #(reset! main-ns (old-in-ns %))]
      (clojure.main/repl
       :init #(in-ns (ns-name *ns*))
       :prompt (fn [] (printf "[watching %s] %s=> " share (ns-name *ns*))))
      (reset! *watching* false))))
             
;; Share implementation
(def content (atom []))

(defn partition-content [content]
  (let [contents (partition-all *max-size* content)
        total (count contents)
        nums (iterate inc 0)]
    (sort #(compare (:order %1) (:order %2))
          (map (fn [c n] {:content (apply str c) :order n :total total})
               contents nums))))
     
(defn share
  "Share your REPL with the passed share name."
  [share]
  (binding [*out* (OutInterceptor. content *out*)
            *err* (ErrInterceptor. content *err*)
            *in* (InInterceptor. content *in*)]
    (clojure.main/repl
     :prompt (fn [] (printf "[%s] %s=> " share (ns-name *ns*)))
     :flush (fn []
              (doseq [{:keys [content order total]} (partition-content @content)]
                (broadcast share content order total))
              (reset! content [])
              (flush)))))