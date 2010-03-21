(ns net.progski.repl-share.BroadcastWriter
  (:import [java.net DatagramPacket InetAddress MulticastSocket])
  (:use [net.progski.repl-share.broadcast])
  (:gen-class
   :extends java.io.Writer
   :init init
   :main false
   :constructors {[String java.io.Writer] []}
   :state state
   :exposes-methods {append appendSuper,
                     write writeSuper}))

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