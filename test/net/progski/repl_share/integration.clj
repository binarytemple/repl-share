(ns net.progski.repl-share.integration
  (:import [java.io PrintWriter StringReader StringWriter])
  (:require [net.progski.repl-share :as rs])
  (:use clojure.test
        net.progski.repl-share.broadcast))

(def mock-content (ref ""))
(def broadcast-count (atom 0))

(defn mock-broadcast [share content order total]
  (dosync (alter mock-content str content))
  (swap! broadcast-count inc))

(defn build-screen [in out]
  (str "[test] user=> " in  out \newline))

(defn share-test [input output]
  (let [input (str input \newline)
        in (StringReader. input)
        out (StringWriter.)
        err (StringWriter.)]
    ;; reload to get namespace back in initial state
    (require :reload-all 'net.progski.repl-share)
    (binding [broadcast mock-broadcast
              *in* in
              *out* out
              *err* (PrintWriter. err)]
      (rs/share "test"))
    (is (= @mock-content (build-screen input output)))
    (when-not (is (or
                   (.contains (str out) output)
                   (.contains (str err) output)))
      (printf "out: %s%n" out)
      (printf "err: %s%n" err))
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

(deftest test-exceptions
  (share-test "(throw (Exception. \"oh noes!\"))"
              "java.lang.Exception: oh noes! (NO_SOURCE_FILE:1)"))

(deftest test-multi-packet
  (let [expected (binding [*out* (StringWriter.)]
                   (prn (range 1000))
                   (.toString *out*))]
    (share-test "(range 1000)"
                (.substring expected 0 (dec (count expected))))
    (is (= 5 @broadcast-count))))
