(ns net.progski.repl-share.broadcast
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

(defn broadcast [share msg order total]
  (let [sock (MulticastSocket. 6789)
        msg* (serialize {:share share
                         :content msg
                         :order order
                         :total total})
        packet (make-packet msg* *group-addr* 6789)]
    (doto sock
      (.send packet))))
