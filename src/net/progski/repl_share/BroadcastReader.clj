(ns net.progski.repl-share.BroadcastReader
  (:gen-class
   :extends clojure.lang.LineNumberingPushbackReader
   :init init
   :main false
   :constructors {[String clojure.lang.Ref java.io.Reader] [java.io.Reader]}
   :state state
   :exposes-methods {read readSuper
                     unread unreadSuper}))

(defn -init [s r in]
  [[in] {:buff (atom [])
         :content r
         :share s
         :in in}])

(defn -read
  ([this]
     (let [{:keys [buff content share in]} (.state this)
           raw (.readSuper this)
           ch (char raw)]
       (if (= ch \newline)
         (do (dosync (alter content str (apply str (conj @buff \newline))))
             (reset! buff []))
         (swap! buff conj ch))
       raw)))

(defn -unread
  ([this c]
     (let [{buff :buff} (.state this)]
       (swap! buff pop)
       (.unreadSuper this c))))