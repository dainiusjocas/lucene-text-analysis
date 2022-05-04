(ns build
  "lt.jocas/lucene-text-analysis's build script.
  clojure -T:build deploy
  For more information, run:
  clojure -A:deps -T:build help/doc"
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'lt.jocas/lucene-text-analysis)
(defn- the-version [patch] (format "1.0.%s" patch))
(def version (the-version (b/git-count-revs nil)))
(def snapshot (the-version "999-SNAPSHOT"))

(def basis (b/create-basis {:project "deps.edn"}))
(def class-dir "classes")

(defn clean [& _]
  (b/delete {:path class-dir}))

(defn compile-java [opts]
  (clean)
  (b/javac {:src-dirs  ["java"]
            :class-dir (or (:class-dir opts) class-dir)
            :basis     basis
            :java-opts ["-source" "11" "-target" "11"]})
  (println "DONE COMPILING TEXT ANALYSIS JAVA DEPS!"))

(defn deploy "Deploy the JAR to Clojars" [opts]
  (bb/clean opts)
  (compile-java {:class-dir "target/classes"})
  (-> opts
      (assoc :lib lib :version (if (:snapshot opts) snapshot version))
      (assoc :src-pom "pom.xml.template")
      (bb/jar)
      (bb/deploy)))

(defn trigger-release [opts]
  (let [tag (str "v" (if (:snapshot opts) snapshot version))]
    (println "Initiating release for git tag:" tag)
    (b/git-process {:git-args ["tag" tag]})
    (b/git-process {:git-args ["push" "origin" tag]})))
