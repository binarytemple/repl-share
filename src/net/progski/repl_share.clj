;; TODOs
;;
;; Send kill signal to watchers.
;;
;; For large content will need to break across multiple
;; datagrams...may want to avoid this all together.
;;
(ns net.progski.repl-share
  (:import [java.net DatagramPacket InetAddress MulticastSocket]))

;; Different group addr?
(def *group-addr* (InetAddress/getByName "228.5.6.7"))

(defn serialize
  "Serialize a Clojure expression to a byte array."
  [expr]
  (binding [*print-dup* true] (.getBytes (pr-str expr))))

(defn deserialize
  "Deserialize a byte array to a Clojure data type."
  [bytes]
  (read-string (String. bytes)))

(defn make-packet
  "Create a UDP packet.  If 1 arg, then it's for receiving. If 2 args,
   then it's for sending."
  ([buf]
     (DatagramPacket. buf (count buf)))
  ([msg addr port]
     (DatagramPacket. msg (count msg) addr port)))

(defn join-group
  "Joing a multicast group."
  ([group] (join-group group 6789))
  ([group port]
     (doto (MulticastSocket. port)
       (.joinGroup group))))



(defn send-to-share [share msg]
  (let [sock (MulticastSocket. 6789)
        msg* (serialize {:share share
                         :content msg})
        packet (make-packet msg* *group-addr* 6789)]
    (doto sock
      (.send packet))))

;; (defn kill-share [share]
;;   (send-to-share share {:kill true}))

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
             
(comment 
  (watch-share "ryan-repl")
  (send-to-share "ryan-repl" "(+ 1 1)"))

;; Start a new, sub-REPL.
(defn share
  "Share your REPL with the passed share name."
  [share]
  (clojure.main/repl
   :prompt (fn [] (printf "[share] %s=> " (ns-name *ns*)))))

(comment :print (fn [o] (broadcast)))