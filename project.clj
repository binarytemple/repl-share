(defproject net.progski/repl-share "0.1-SNAPSHOT"
  :description "Share your REPL over the LAN!"
  :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0-SNAPSHOT"]]
  :dev-dependencies [[swank-clojure/swank-clojure "1.2.1"]]
  :namespaces [net.progski.repl-share.InInterceptor,
               net.progski.repl-share.ErrInterceptor,
               net.progski.repl-share.OutInterceptor])