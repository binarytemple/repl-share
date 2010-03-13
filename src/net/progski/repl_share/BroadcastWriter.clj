(ns net.progski.repl-share.BroadcastWriter
  (:import [java.net DatagramPacket InetAddress MulticastSocket])
  (:gen-class
   :extends java.io.Writer
   :init init
   :main false
   :constructors {[String java.io.Writer] []}
   :state state
   :exposes-methods {append appendSuper,
                     write writeSuper}))


(def *group-addr* (InetAddress/getByName "228.5.6.7"))

(defn make-packet
  "Create a UDP packet.  If 1 arg, then it's for receiving. If 2 args,
   then it's for sending."
  ([buf]
     (DatagramPacket. buf (count buf)))
  ([msg addr port]
     (DatagramPacket. msg (count msg) addr port)))

(defn serialize
  "Serialize a Clojure expression to a byte array."
  [expr]
  (binding [*print-dup* true] (.getBytes (pr-str expr))))

(defn deserialize
  "Deserialize a byte array to a Clojure data type."
  [bytes]
  (read-string (String. bytes)))

(defn broadcast [share msg]
  (let [sock (MulticastSocket. 6789)
        msg* (serialize {:share share
                         :content msg})
        packet (make-packet msg* *group-addr* 6789)]
    (doto sock
      (.send packet))))

(defn -init [s out]
  [[] {:buff (atom [])
       :share s
       :out out}])

(defn -write
  ([this arg] (.writeSuper this arg))
  ([this ch-arr off len]
     (let [{:keys [out buff]} (.state this)]
       (swap! buff (partial apply conj) (subvec (vec ch-arr) off (+ off len)))
       (.write out ch-arr off len))))

(defn -flush [this]
  (let [{:keys [share out buff]} (.state this)]
    (broadcast share (apply str @buff))
    (reset! buff [])
    (.flush out)))

(defn -close [this]
  (let [{out :out} (.state this)]
    (.close out)))

