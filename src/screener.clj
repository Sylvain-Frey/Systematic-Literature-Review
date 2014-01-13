(ns screener)
(require '[clojure.data.csv :as csv])
(require '[clojure.java.io :as io])
(require '[clojure.string :as string])

(def separator \,) ; could be: \tab \, \;
(def title-column 3)    ; beware: clojure list indexes
(def abstract-column 4) ; start at 0
(def csv-input (nth *command-line-args* 0))
(def csv-output (nth *command-line-args* 1))

(defn read-input [file] 
  (doall 
    (csv/read-csv file :separator separator)))

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

(defn review [input out-file]
  (doseq [line input]
    (println "\n\n\n")
    (println "Title: " (nth line title-column) "\n")
    (println "Abstract: " (nth line abstract-column) "\n")
    (println "Accept/Borderline/Reject (A/B/R)?")
    (let [status (read-status (read-line))] 
      (println "Additional Comments?")
      (let [comments (read-line)]
        (write-output out-file [(conj line status comments)])))))

(defn screen [in-file out-file] 
  (let [input (read-input in-file)]
    (do
      (write-headers (first input) out-file)
      (println "\nReview started..." "\n")
      (review (rest input) out-file))))

(with-open [out-file (io/writer csv-output)
            in-file (io/reader csv-input)] 
  (screen in-file out-file))

