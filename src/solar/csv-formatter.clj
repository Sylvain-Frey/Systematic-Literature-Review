;; Formatting script for CSV metadata for different input formats.
;; Supported input formats: IEEE CSV, Springer CSV, 
;; ISI Web of Science, self-defined default CSV.
;; Output format: self-defined CSV.

(ns solar.csv-formatter
  [:require [clojure.java.io :as io]]
  [:require [clojure.string :as string]]
  [:require [net.cgrand.enlive-html :as html]]
  [:require [clojure.data.csv :as csv]]
  [:require [solar.templates :as templates]])


;; check commmand line arguments

(if (not= (count *command-line-args*) 4)
  (do
    (println)
    (println "Usage: csv-formatter <input-file> <output-file> <format> <query>")
    (println)
    (println "Examples: csv-formatter ieee.csv ieee.formatted.csv ieee my_query")
    (println "          csv-formatter springer.csv springer.formatted.csv springer my_query")
    (println "          csv-formatter isiwos.csv isiwos.formatted.csv isiwos my_query")
    (println)
    (System/exit 0)))

(def input (nth *command-line-args* 0))
(def output (nth *command-line-args* 1))
(def template-name (nth *command-line-args* 2))
(def search-term (nth *command-line-args* 3))

(def current-template 
  (case template-name
    "ieee" templates/ieee
    "springer" templates/springer
    "isiwos" templates/isiwos
    templates/default))


;; IO functions

(defn parse [tag line] 
  ((current-template :parse) line tag))

(defn read-input [file] 
  (csv/read-csv file :separator (current-template :separator)))

(defn write-output [file output]
  (csv/write-csv file output))

(defn write-headers [out-file]
  (write-output out-file [templates/default-headers]))
  
  
;; main steps
  
(defn format* [input output]
  (let [line (first input)] 
    (if line
      (let [id (current-template :generate-id-from line)]
        (write-output output [[ 
                   ((current-template :generate-id-from) line)
                   (parse :year line)
                   (parse :authors line)
                   (parse :title line)
                   (parse :abstract line)
                   template-name
                   search-term
                   (parse :url line)]])
        (recur (rest input) output)))))

(with-open [out-file (io/writer output)
            in-file (io/reader input)]
  (let [input (read-input in-file)]
    (write-headers out-file)
    (format*  (rest input) out-file)))
    