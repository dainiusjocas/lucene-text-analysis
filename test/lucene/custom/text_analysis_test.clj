(ns lucene.custom.text-analysis-test
  (:require [clojure.test :refer [deftest is testing]]
            [lucene.custom.text-analysis :as text-analysis])
  (:import (org.apache.lucene.analysis.standard StandardAnalyzer)))

(deftest text-analysis-api
  (testing "text to strings"
    (is (= ["test" "text"]
           (text-analysis/text->token-strings "Test TEXT" (StandardAnalyzer.)))))

  (testing "text to tokens"
    (is (= [{:token "test", :type "<ALPHANUM>", :start_offset 0, :end_offset 4, :position 0, :positionLength 1}
            {:token "text", :type "<ALPHANUM>", :start_offset 5, :end_offset 9, :position 1, :positionLength 1}]
           (map (fn [m] (into {} m)) (text-analysis/text->tokens "Test TEXT" (StandardAnalyzer.))))))

  (testing "text to graph"
    (is (string? (text-analysis/text->graph "text to tokens" (StandardAnalyzer.))))))

(deftest nil-handling
  (testing "empty string and nil are equal"
    (is (= (text-analysis/text->token-strings "")
           (text-analysis/text->token-strings nil)))
    (is (= (text-analysis/text->tokens "")
           (text-analysis/text->tokens nil)))
    (is (= (text-analysis/text->graph "")
           (text-analysis/text->graph nil)))))
