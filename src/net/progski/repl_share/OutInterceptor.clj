(ns net.progski.repl-share.OutInterceptor
  (:gen-class
   :extends java.io.Writer
   :init init
   :main false
   :constructors {[clojure.lang.Atom java.io.Writer] []}
   :state state
   :exposes-methods {write writeSuper}))

(defn -init [r out]
  [[] {:buff (atom [])
       :content r
       :out out}])

(defn -write
  ([this arg] (.writeSuper this arg))
  ([this ch-arr off len]
     (let [{:keys [out buff]} (.state this)]
       (swap! buff (partial apply conj) (subvec (vec ch-arr) off (+ off len)))
       (.write out ch-arr off len))))

(defn -flush [this]
  (let [{:keys [content buff out]} (.state this)]
    (swap! content (partial apply conj) @buff)
    (reset! buff [])
    (.flush out)))

(defn -close [this]
  (let [{out :out} (.state this)]
    (.close out)))