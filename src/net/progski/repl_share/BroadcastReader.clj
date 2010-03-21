(ns net.progski.repl-share.BroadcastReader
  (:import [java.net DatagramPacket InetAddress MulticastSocket])
  (:use [net.progski.repl-share.broadcast])
  (:gen-class
   :extends clojure.lang.LineNumberingPushbackReader
   :init init
   :main false
   :constructors {[String java.io.Reader] [java.io.Reader]}
   :state state
   :exposes-methods {read readSuper
                     unread unreadSuper}))

(defn -init [s in]
  [[in] {:buff (atom [])
         :share s
         :in in}])

(defn -read
  ([this]
     (let [{:keys [buff share in]} (.state this)
           ch (char (.readSuper this))]
       (when (= ch \newline)
         (broadcast share (apply str @buff))
         (reset! buff []))
       (swap! buff conj ch)
       (int ch))))

(defn -unread
  ([this c]
     (let [{buff :buff} (.state this)]
       (swap! buff pop)
       (.unreadSuper this c))))