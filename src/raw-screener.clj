;; Screener script for csv metadata for different input formats.
;; Supported formats: IEEE, Springer, self-defined default.

(ns ieee-fast-screener)
(require '[clojure.data.csv :as csv])
(require '[clojure.java.io :as io])
(require '[clojure.string :as string])

;; Parsing templates, define what column
;; contains relevant data in the input csv.

(def ieee-template {
  :separator \,
  :year 5
  :authors 1
  :title 0
  :abstract 10
  :url 15
})


(def default-template { 
  :separator \,
  :year 1
  :authors 2
  :title 3
  :abstract 4
  :url 7
})


(def current-template 
  (case (nth *command-line-args* 2) 
    "ieee" ieee-template
    default-template))

(defn parse [tag line] 
  (nth line (current-template tag)))

;; IO functions.

(def csv-input (nth *command-line-args* 0))
(def csv-output (nth *command-line-args* 1))

(defn read-input [file] 
  (doall 
    (csv/read-csv file :separator (current-template :separator))))

(defn write-output [file output]
  (csv/write-csv file output))

(defn write-headers [input-head out-file]
  (write-output out-file [(conj input-head "Status" "Comments")]))

(defn read-status [status]
  (case (string/lower-case status)
    "a" "Accepted"
    "r" "Rejected"
    "b" "Borderline"
    status))


;; Main functions.
    
(defn review [input counter total out-file]
  (let [line (first input)] 
    (when line 
      (do
        (println "\n\n\n")
        (println "Paper: " counter "/" total "\n")
        (println "Year: " (parse :year line) "\n")
        (println "Authors: " (parse :authors line) "\n")
        (println "Title: " (parse :title line) "\n\n")
        (println "Abstract: " (parse :abstract line) "\n\n")
        (println "URL: " (parse :url line) "\n\n")
        (println "Accept/Borderline/Reject (A/B/R)?")
        (let [status (read-status (read-line))] 
          (println "Additional Comments?")
          (let [comments (read-line)]
            (write-output out-file [(conj line status comments)]))))
        (recur (rest input) (inc counter) total out-file))))

(defn screen [in-file out-file] 
  (let [input (read-input in-file)]
    (do
      (write-headers (first input) out-file)
      (review (rest (rest input)) 1 (- (count input) 2) out-file))))

(with-open [out-file (io/writer csv-output)
            in-file (io/reader csv-input)] 
  (screen in-file out-file))

