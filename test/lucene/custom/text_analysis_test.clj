(ns lucene.custom.text-analysis-test
  (:require [clojure.test :refer [deftest is testing]]
            [lucene.custom.text-analysis :as text-analysis])
  (:import (org.apache.lucene.analysis.standard StandardAnalyzer)
           (org.apache.lucene.analysis.miscellaneous PerFieldAnalyzerWrapper)
           (org.apache.lucene.analysis.core KeywordAnalyzer)))

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

(deftest per-field-analyzer
  (testing "if field-name is handled with PerFieldAnalyzerWrapper in text->token-strings"
    (let [analyzer (PerFieldAnalyzerWrapper. (KeywordAnalyzer.) {"field" (StandardAnalyzer.)})]
      (is (= ["Test TEXT"] (text-analysis/text->token-strings "Test TEXT" analyzer)))
      (is (= ["test" "text"] (text-analysis/text->token-strings "Test TEXT" analyzer "field")))))

  (testing "if field-name is handled with PerFieldAnalyzerWrapper in text->tokens"
    (let [analyzer (PerFieldAnalyzerWrapper. (KeywordAnalyzer.) {"field" (StandardAnalyzer.)})]
      (is (= 1 (count (text-analysis/text->tokens "Test TEXT" analyzer))))
      (is (= 2 (count (text-analysis/text->tokens "Test TEXT" analyzer "field"))))))

  (testing "if field-name is handled with PerFieldAnalyzerWrapper in text->"
    (let [analyzer (PerFieldAnalyzerWrapper. (KeywordAnalyzer.) {"field" (StandardAnalyzer.)})]
      (is (< (count (text-analysis/text->graph "Test TEXT" analyzer))
             (count (text-analysis/text->graph "Test TEXT" analyzer "field")))))))
