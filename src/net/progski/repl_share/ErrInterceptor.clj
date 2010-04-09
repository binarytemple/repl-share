(ns net.progski.repl-share.ErrInterceptor
  (:gen-class
   :extends java.io.PrintWriter
   :init init
   :main false
   :constructors {[clojure.lang.Atom java.io.PrintWriter] [java.io.Writer boolean]}
   :state state
   :exposes-methods {println printlnSuper}))

(defn -init [content err]
     [[err true] {:content content
                  :err err}])

(defn -println [this #^Object x]
  (let [{content :content err :err} (.state this)]
    (swap! content (partial apply conj) (seq (str x \newline)))
    (.println err x)
    (.flush err)))