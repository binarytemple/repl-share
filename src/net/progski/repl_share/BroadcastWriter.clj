(ns net.progski.repl-share.BroadcastWriter
  (:gen-class
   :extends java.io.Writer
   :init init
   :main false
   :constructors {[String clojure.lang.Ref java.io.Writer] []}
   :state state
   :exposes-methods {write writeSuper}))

(defn -init [s r out]
  [[] {:buff (atom [])
       :content r
       :share s
       :out out}])

(defn -write
  ([this arg] (.writeSuper this arg))
  ([this ch-arr off len]
     (let [{:keys [out buff]} (.state this)]
       (swap! buff (partial apply conj) (subvec (vec ch-arr) off (+ off len)))
       (.write out ch-arr off len))))

(defn -flush [this]
  (let [{:keys [share content out buff]} (.state this)]
    (dosync (alter content str (apply str @buff)))
    (reset! buff [])
    (.flush out)))

(defn -close [this]
  (let [{out :out} (.state this)]
    (.close out)))