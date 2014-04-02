;; Parse a CSV file following the "default" format,
;; and look for duplicate paper IDs.
;; Warning: due to different - and sometimes buggy (Springer) -
;; author formattings, the duplicate hunt may fail.


(ns solar.duplicate-filter
  [:require [solar.io :as io]]
  [:require [clojure.java.io]]
  [:require [clojure.string :as string]]
  [:require [solar.templates :as templates]])


;; Check command line arguments.

(io/check-args
  ['input 'output]
  "
  Usage: duplicate-filter <input-file> <output-file>

  Example: duplicate-filter acm.formatted.csv acm.filtered.csv")

(def template (templates/for-name "default"))



;; Filter functions.
 
(defn generate-accurate-id-from [line]
  (string/join "-" [
    (string/replace ((template :parse) :year line) #" " "")
    (string/replace 
      (string/lower-case 
       (string/replace ((template :parse) :title line)  #" " "")) 
    #"[^a-z]" "")
  ]))
 
(defn filter* [input ids output]
  (let [line (first input)] 
    (if line
      (let [id (generate-accurate-id-from line)]
        (if (contains? ids id)
          (recur (rest input) (assoc ids id (+ (get ids id) 1)) output)
          (do
            (io/write-csv output line)
            (recur (rest input) (assoc ids id 1) output))))
      ids)))


;; Main function.

(with-open [out-file (clojure.java.io/writer output)
            in-file (clojure.java.io/reader input)]
  (io/write-default-headers out-file)
  (let [input ((template :read) in-file)
        raw-ids (filter* (rest input) (hash-map) out-file)
        duplicates (filter #(> (second %) 1) raw-ids)]
    (println "Input file contained" (reduce + 0 (vals raw-ids)) "references")
    (println "Found" (- (reduce + 0 (vals duplicates)) (count duplicates)) "duplicates:")
    (println duplicates)
    (println "Formatted result contains" (count raw-ids) "references")))
    