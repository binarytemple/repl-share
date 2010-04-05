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
  [[in] {:content r
         :share s}])

(defn -read
  ([this]
     (let [{:keys [content share]} (.state this)
           raw (.readSuper this)
           ch (char raw)]
       (if (= ch \newline)
         (dosync (alter content str \newline))
         (dosync (alter content str ch)))
       raw)))

(defn -unread
  ([this c]
     (let [{content :content} (.state this)]
       (dosync (alter content #(.substring % 0 (- (count %) 1))))
       (.unreadSuper this c))))