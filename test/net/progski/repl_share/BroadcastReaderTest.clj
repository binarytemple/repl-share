(ns net.progski.repl-share.BroadcastReaderTest
  (:use [net.progski.repl-share.broadcast :only (broadcast)])
  (:use clojure.test)
  (:import net.progski.repl_share.BroadcastReader))

(declare *br*)
(def *broadcast-flag* (atom false))
(def *broadcast-content* (atom ""))

;; Helpers
(defn buff-str []
  (apply str @(:buff (.state *br*))))

(defn mock-broadcast [_ content]
  (reset! *broadcast-content* content)
  (reset! *broadcast-flag* true))

;; Fixtures
(defn br-fixture [f]
  (binding [*br* (BroadcastReader. "test" (java.io.StringReader. "ryan\n"))]
    (f)))

(defn broadcast-fixture [f]
  (reset! *broadcast-content* "")
  (reset! *broadcast-flag* false)
  (binding [broadcast mock-broadcast]
    (f)))

(use-fixtures :each br-fixture broadcast-fixture)

;; Test cases
(deftest test-read
  (dotimes [_ 4] (.read *br*))
  (is (= "ryan" (buff-str)))
  (.read *br*)
  ;; It is important that a newline be added to the broadcasted content
  (is (= "ryan\n" @*broadcast-content*))
  (is (empty? (buff-str)))
  (is @*broadcast-flag*))

(deftest test-unread
  (let [ch (.read *br*)]
    (is (= "r" (buff-str)))
    (.unread *br* ch)
    (is (empty? (buff-str)))
    (.read *br*)
    (is (= "r" (buff-str)))))