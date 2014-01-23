;; Screener script for CSV metadata for different input formats.
;; Supported input formats: IEEE, Springer, self-defined default.

(ns solar.screener
  [:require [clojure.data.csv :as csv]]
  [:require [clojure.java.io :as io]]
  [:require [clojure.string :as string]]
  [:require [clojure.repl :as repl]]
  [:require [net.cgrand.enlive-html :as html]]
  [:require [solar.templates :as templates]])


;; check command line arguments

(if (not= (count *command-line-args*) 3)
  (do
    (println)
    (println "Usage: screener <input-file> <output-file> <format>")
    (println)
    (println "Examples: screener ieee.csv ieee.screened.csv ieee")
    (println "          screener springer.csv springer.screened.csv springer")
    (println "          screener acm.csv acm.screened.csv default")
    (println)
    (System/exit 0)))

(def csv-input (nth *command-line-args* 0))
(def csv-output (nth *command-line-args* 1))
(def current-template 
  (case (nth *command-line-args* 2) 
    "ieee" templates/ieee
    "springer" templates/springer
    templates/default))


;; IO functions.

(defn parse [tag line] 
  ((current-template :parse) line tag))

(defn read-input [file] 
  (csv/read-csv file :separator (current-template :separator)))

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
 
(defn print-exit-instructions []
  (println "Hit Ctrl-D to save and exit."))



;; Main functions.
    
(defn review [input counter total out-file]
  (let [line (first input)] 
    (when line 
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
          (write-output out-file [(conj line status comments)])))
      (recur (rest input) (inc counter) total out-file))))

(defn screen [in-file out-file] 
  (let [input (read-input in-file)]
    (write-headers (first input) out-file)
    (try 
      (review (rest (rest input)) 1 (- (count input) 2) out-file)
      (catch Exception e (println "Review interrupted.")))))

(with-open [out-file (io/writer csv-output)
            in-file (io/reader csv-input)] 
  (print-exit-instructions)
  (repl/set-break-handler! (fn [sig] (print-exit-instructions)))
  (screen in-file out-file))