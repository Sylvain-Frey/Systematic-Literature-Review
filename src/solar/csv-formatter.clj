;; Formatting script for CSV metadata for different input formats.
;; Supported input formats: IEEE CSV, Springer CSV, 
;; ISI Web of Science, self-defined default CSV.
;; Output format: self-defined CSV.

(ns solar.csv-formatter
  [:require [solar.io :as io]]
  [:require [clojure.java.io]]
  [:require [clojure.string :as string]]
  [:require [solar.templates :as templates]])


;; Check command line arguments.

(io/check-args
  ['input 'output 'template-name 'search-term]
  "
  Usage: csv-formatter <input-file> <output-file> <format> <query>

  Examples: csv-formatter ieee.csv ieee.formatted.csv ieee my_query
            csv-formatter springer.csv springer.formatted.csv springer my_query
            csv-formatter isiwos.csv isiwos.formatted.csv isiwos my_query")

(def template (templates/for-name template-name))


  
;; Main functions.

(defn format* [input output]
  (if-let [line (first input)] 
    (let [id (template :generate-id-from line)]
      (io/write-csv output [ 
                            ((template :generate-id-from) line)
                            ((template :parse) :year line)
                            ((template :parse) :authors line)
                            ((template :parse) :title line)
                            ((template :parse) :abstract line)
                            template-name
                            search-term
                            ((template :parse) :url line)])
      (recur (rest input) output))))

(with-open [out-file (clojure.java.io/writer output)
            in-file  (clojure.java.io/reader input)]
  (let [input ((template :read) in-file)]
    (io/write-default-headers out-file)
    (format*  (rest input) out-file)))
    
