;; Parse a CSV file following the "default" format,
;; and look for duplicate paper IDs.
;; Warning: due to different - and sometimes buggy (Springer) -
;; author formattings, the duplicate hunt may fail.


(ns solar.duplicate-filter
  [:require [clojure.java.io :as io]]
  [:require [clojure.string :as string]]
  [:require [clojure.data.csv :as csv]]
  [:require [solar.templates :as templates]])

;; check commmand line arguments

(if (not= (count *command-line-args*) 2)
  (do
    (println)
    (println "Usage: duplicate-filter <input-file> <output-file>")
    (println)
    (println "Example: duplicate-filter acm.formatted.csv acm.filtered.csv")
    (println)
    (System/exit 0)))

(def input (nth *command-line-args* 0))
(def output (nth *command-line-args* 1))

(def current-template templates/default)


;; IO functions

(defn parse [tag line] 
  ((current-template :parse) line tag))

(defn read-input [file] 
  (csv/read-csv file :separator (current-template :separator)))

(defn write-output [file output]
  (csv/write-csv file output))

(defn write-headers [out-file]
  (write-output out-file [templates/default-headers]))


;; filter functions
 
(defn generate-accurate-id-from [line]
  (string/join "-" [
    (.substring (parse :year line) 2 4)
    (parse :authors line)
    (parse :title line)
    ]))
 
(defn format* [input ids output]
  (let [line (first input)] 
    (if line
      (let [id (generate-accurate-id-from line)]
        (if (contains? ids id)
          (recur (rest input) (assoc ids id (+ (get ids id) 1)) output)
          (do
            (write-output output [line])
            (recur (rest input) (assoc ids id 1) output))))
      ids)))

(with-open [out-file (io/writer output)
            in-file (io/reader input)]
  (write-headers out-file)
  (let [input (read-input in-file)
        raw-ids (format* (rest input) (hash-map) out-file)
        duplicates (filter #(> (second %) 1) raw-ids)]
    (println "Input file contained" (reduce + 0 (vals raw-ids)) "references")
    (println "Found" (- (reduce + 0 (vals duplicates)) (count duplicates)) "duplicates:")
    (println duplicates)
    (println "Formatted result contains" (count raw-ids) "references")))
    