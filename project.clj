(defproject net.progski/repl-share "0.1-SNAPSHOT"
  :description "Share your REPL over the LAN!"
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]]
  :dev-dependencies [[leiningen/lein-swank "1.1.0"]]
  :namespaces [net.progski.repl-share.BroadcastWriter,
               net.progski.repl-share.BroadcastReader])