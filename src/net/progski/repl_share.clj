;; TODOs
;;
;; Send kill signal to watchers.
;;
;; For large content will need to break across multiple
;; datagrams...may want to avoid this all together.
;;
(ns net.progski.repl-share
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io StringReader]
           [net.progski.repl_share BroadcastWriter])
  (:use [net.progski.repl-share.broadcast]))

(def buf (byte-array 1000))

(def *watching* (atom false))

(defn watch-share
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
             
;; Relies on the fact that .readLine will return the empty string for
;; CR and nil for EOT.
(defn make-reader [share]
  (fn [request-prompt request-exit]
    (let [line (.readLine *in*)]
      (broadcast share line)
      (cond (nil? line)
            request-exit
            (empty? line)
            request-prompt
            :else
            (do (binding [*in* (LineNumberingPushbackReader. (StringReader. line))]
                  (clojure.main/repl-read request-prompt request-exit)))))))

;; Start a new, sub-REPL.
(defn share
  "Share your REPL with the passed share name."
  [share]
  (binding [*out* (BroadcastWriter. share *out*)]
    (clojure.main/repl
     :prompt (fn [] (printf "[share] %s=> " (ns-name *ns*)))
     :read (make-reader share))))