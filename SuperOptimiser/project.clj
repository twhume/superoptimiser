(defproject SuperOptimiser "0.0.1"
  :description "Exhaustive searches for optimal JVM bytecode, while u wait"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.ow2.asm/asm "4.0"]
                 [org.ow2.asm/asm-tree "4.0"]
                 [lein-eclipse "1.0.0"]
                 [noir "1.3.0-beta1"]
                 [clj-http "0.5.2"]
                 [org.clojure/tools.logging "0.2.3"]
                 [clojure-csv/clojure-csv "2.0.0-alpha1"]
                 [org.clojure/math.combinatorics "0.0.2"]
                 [org.clojure/data.priority-map "0.0.1"]])
