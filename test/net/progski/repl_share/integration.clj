(ns net.progski.repl-share.integration
  (:import [java.io StringReader StringWriter])
  (:require [net.progski.repl-share :as rs])
  (:use clojure.test
        net.progski.repl-share.broadcast))

(def mock-content (ref ""))

(defn mock-broadcast [share content]
  (dosync (alter mock-content str content)))

(defn build-screen [in out]
  (str "[test] user=> " in  out \newline))

(defn share-test [in out]
  (let [in (str in \newline)]
    ;; reload to get namespace back in initial state
    (require :reload-all 'net.progski.repl-share)
    (binding [broadcast mock-broadcast
              *in* (StringReader. in)
              *out* (StringWriter.)]
      (rs/share "test"))
    (is (= @mock-content (build-screen in out)))
    (dosync (ref-set mock-content ""))))

(deftest test-datastructures
  (share-test "1" "1")
  (share-test "\"ryan\"" "\"ryan\"")
  (share-test "{:name \"ryan\"}" "{:name \"ryan\"}")
  (share-test ":ryan" ":ryan")
  (share-test "'ryan" "ryan")
  (share-test "[1 2 3]" "[1 2 3]")
  (share-test "#{:foo :bar}" "#{:foo :bar}")
  (share-test "'()" "()")
  (share-test "\\r" "\\r")
  (share-test "nil" "nil"))

(deftest test-multiline
  (share-test "\"ry\nan\"" "\"ry\\nan\"")
  (share-test "(str \"hello\" \" world\")" "\"hello world\"")
  (share-test "{:name\n\"ryan\"}" "{:name \"ryan\"}")
  (share-test "#{5\n6}" "#{5 6}")
  (share-test "(+ 1\n2\n3)" "6"))

(deftest test-defs
  (share-test "(def x 6)" "#'user/x")
  (share-test "(defn sq [x] (* x x))" "#'user/sq"))
