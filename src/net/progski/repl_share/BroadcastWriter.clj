(ns net.progski.repl-share.BroadcastWriter
  (:gen-class
   :extends java.io.Writer
   :init init
   :main false
   :constructors {[String] []}
   :state state
   :exposes-methods {append super-append}))

(defn -init [s]
  [[] {:share-name s}])

(defn -append [this c]
  (.super-append this c))