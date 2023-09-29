(ns lucene.custom.text-analysis-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [lucene.custom.analyzer-wrappers :as analyzer]
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
             (count (text-analysis/text->graph "Test TEXT" analyzer "field"))))))

  (testing "if field-name is handled with PerFieldAnalyzerWrapper in text->"
    (let [field-name :field
          analyzer (analyzer/->per-field-analyzer-wrapper {:tokenizer :keyword} {field-name {}})]
      (is (= ["Test TEXT"] (text-analysis/text->token-strings "Test TEXT" analyzer)))
      (is (= ["Test" "TEXT"] (text-analysis/text->token-strings "Test TEXT" analyzer field-name)))
      (is (= 2 (count (text-analysis/text->tokens "Test TEXT" analyzer field-name))))
      (is (string? (text-analysis/text->graph "Test TEXT" analyzer field-name))))))

(deftest multifield-analysis
  (let [doc {:field-a "foo bar baz"
             :field-b "aa bb"
             :field-c "Apache Lucene"}
        analyzer (analyzer/->per-field-analyzer-wrapper
                   {:tokenizer :keyword}
                   {:field-a {:token-filters [:reverseString]}
                    :field-b {:token-filters [:uppercase]}})]
    (testing "doc->token-strings"
      (is (= {:field-a ["oof" "rab" "zab"]
              :field-b ["AA" "BB"]
              :field-c ["Apache Lucene"]}
             (text-analysis/doc->token-strings doc analyzer))))
    (testing "doc->tokens"
      (is (= {:end_offset     13
              :position       0
              :positionLength 1
              :start_offset   0
              :token          "Apache Lucene"
              :type           "word"}
             (into {} (:field-c (text-analysis/doc->tokens doc analyzer))))))
    (testing "doc->graph"
      (is (str/starts-with?
            (:field-a (text-analysis/doc->graph doc analyzer)) "digraph")))))
