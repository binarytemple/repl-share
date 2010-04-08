(ns net.progski.repl-share.BroadcastWriterTest
  (:use [net.progski.repl-share.broadcast :only (broadcast)])
  (:use clojure.test)
  (:import net.progski.repl_share.BroadcastWriter))

(declare *bw*)
(def content (atom []))

;; Helpers
(defn buff-str []
  (apply str @(:buff (.state *bw*))))

;; Fixtures
(defn bw-fixture [f]
  (binding [*bw* (BroadcastWriter. "test" content (java.io.StringWriter.))]
    (f)))

(use-fixtures :each bw-fixture)

;; Test cases
(deftest test-write
  (.write *bw* (char-array "foo"))
  (is (= "foo" (buff-str)))
  (is (empty? @content)))

(deftest test-flush
  (.write *bw* (char-array "ryan"))
  (.flush *bw*)
  (is (empty? (buff-str)))
  (is (= "ryan" (apply str @content))))