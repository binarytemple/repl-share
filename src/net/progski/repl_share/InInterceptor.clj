(ns net.progski.repl-share.InInterceptor
  (:gen-class
   :extends clojure.lang.LineNumberingPushbackReader
   :init init
   :main false
   :constructors {[String clojure.lang.Atom java.io.Reader] [java.io.Reader]}
   :state state
   :exposes-methods {read readSuper
                     unread unreadSuper}))

(defn -init [s r in]
  [[in] {:content r
         :share s}])

(defn -read
  ([this]
     (let [{:keys [content share]} (.state this)
           c (.readSuper this)]
       (if (= c -1)
         nil
         (swap! content conj (char c)))
       c)))

(defn -unread
  ([this c]
     (let [{content :content} (.state this)]
       (swap! content pop)
       (.unreadSuper this c))))