(ns net.progski.repl-share.InInterceptorTest
  (:use [net.progski.repl-share.broadcast :only (broadcast)])
  (:use clojure.test)
  (:import net.progski.repl_share.InInterceptor))

(declare *br*)
(def content (atom []))

;; Fixtures
(defn br-fixture [f]
  (binding [*br* (InInterceptor. "test" content (java.io.StringReader. "ryan\n"))]
    (f))
  (dosync (reset! content [])))

(use-fixtures :each br-fixture)

;; Test cases
(deftest test-read
  (is (= (int \r) (.read *br*)))
  (dotimes [_ 4] (.read *br*))
  (is (= "ryan\n" (apply str @content))))

(deftest test-unread
  (let [ch (.read *br*)]
    (is (= "r" (apply str @content)))
    (.unread *br* ch)
    (is (empty? @content))
    (.read *br*)
    (is (= "r" (apply str @content)))))