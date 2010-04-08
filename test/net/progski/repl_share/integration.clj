(ns net.progski.repl-share.integration
  (:import [java.io StringReader StringWriter])
  (:require [net.progski.repl-share :as rs])
  (:use clojure.test
        net.progski.repl-share.broadcast))

(def mock-content (ref ""))

(def print-it
     (let [*old-out* *out*]
       (fn [s]
         (.write *old-out* s))))

(defn mock-broadcast [share content]
  (dosync (alter mock-content str content)))

(defn build-screen [in out]
  (str "[test] user=> " in  out \newline))

(defn share-test [in out]
  (let [in (str in \newline)]
    (require :reload-all 'net.progski.repl-share)
    (binding [broadcast mock-broadcast
              *in* (StringReader. in)
              *out* (StringWriter.)]
      (rs/share "test"))
    (is (= @mock-content (build-screen in out)))
    (dosync (ref-set mock-content ""))))

(deftest test-numbers
  (share-test "1" "1")
  (share-test "+1" "1")
  (share-test "-1" "-1")
  (share-test "(+ 1 2 3)" "6")
  (share-test "(+ 1\n2\n3)" "6"))

(deftest test-strings
  (share-test "\"ryan\"" "\"ryan\"")
  (share-test "\"ry\nan\"" "\"ry\\nan\"")
  (share-test "(str \"hello\" \" world\")" "\"hello world\""))

(deftest test-keywords
  (share-test ":ryan" ":ryan"))

(deftest test-maps
  (share-test "{:name \"ryan\"}" "{:name \"ryan\"}")
  (share-test "{:name\n\"ryan\"}" "{:name \"ryan\"}"))

(deftest test-sets
  (share-test "#{5\n6}" "#{5 6}"))

(deftest test-defs
  (share-test "(def x 6)" "#'user/x")
  (share-test "(defn sq [x] (* x x))" "#'user/sq"))

(deftest test-other
  (share-test "()" "()")
  (share-test "nil" "nil"))