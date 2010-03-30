(ns net.progski.repl-share.BroadcastWriterTest
  (:use clojure.test)
  (:import net.progski.repl_share.BroadcastWriter))

(declare *bw*)

(defn bw-fixture [f]
  (binding [*bw* (BroadcastWriter. "test" (java.io.StringWriter.))]
    (f)))

(use-fixtures :each bw-fixture)

(defn buff-str []
  (apply str @(:buff (.state *bw*))))

(deftest write
  (.write *bw* (char-array "4\n[share] user=> "))
  (is (= "4\n[share] user=> " (buff-str))))