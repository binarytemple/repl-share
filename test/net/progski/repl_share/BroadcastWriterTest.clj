(ns net.progski.repl-share.BroadcastWriterTest
  (:use [net.progski.repl-share.broadcast :only (broadcast)])
  (:use clojure.test)
  (:import net.progski.repl_share.BroadcastWriter))

(declare *bw*)
(def *broadcast-flag* (atom false))

;; Helpers
(defn buff-str []
  (apply str @(:buff (.state *bw*))))

(defn mock-broadcast [_ _]
  (reset! *broadcast-flag* true))

;; Fixtures
(defn bw-fixture [f]
  (binding [*bw* (BroadcastWriter. "test" (java.io.StringWriter.))]
    (f)))

(defn broadcast-fixture [f]
  (reset! *broadcast-flag* false)
  (binding [broadcast mock-broadcast]
    (f)))

(use-fixtures :each bw-fixture broadcast-fixture)

;; Test cases
(deftest test-write
  (.write *bw* (char-array "foo"))
  (is (= "foo" (buff-str))))

(deftest test-flush
  (binding [broadcast mock-broadcast]
    (.flush *bw*)
    (is @*broadcast-flag*)
    (is (empty? (buff-str)))))