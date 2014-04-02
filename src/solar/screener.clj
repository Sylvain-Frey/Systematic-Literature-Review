;; Screener script for CSV metadata for different input formats.
;; Supported input formats: IEEE, Springer, self-defined default.

(ns solar.screener
  [:require [solar.io :as io]]
  [:require [clojure.java.io]]
  [:require [clojure.string :as string]]
  [:require [clojure.repl :as repl]]
  [:require [net.cgrand.enlive-html :as html]]
  [:require [solar.templates :as templates]])


;; Check command line arguments.

(io/check-args
  ['input 'output 'template-name]
  "
  Usage: screener <input-file> <output-file> <format>
  
  Examples: screener ieee.csv ieee.screened.csv ieee
            screener springer.csv springer.screened.csv springer   
            screener acm.csv acm.screened.csv default")

;; I/O functions.

(def template (templates/for-name template-name))

(defn write-headers [input-head out-file]
  (io/write-csv out-file (conj input-head "Status" "Comments")))

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
      (println "Year: " ((template :parse) :year line) "\n")
      (println "Authors: " ((template :parse) :authors line) "\n")
      (println "Title: " ((template :parse) :title line) "\n\n")
      (println "Abstract: " ((template :parse) :abstract line) "\n\n")
      (println "URL: " ((template :parse) :url line) "\n\n")
      (println "Accept/Borderline/Reject (A/B/R)?")
      (let [status (read-status (read-line))] 
        (println "Additional Comments?")
        (let [comments (read-line)]
          (io/write-csv out-file (conj line status comments))))
      (recur (rest input) (inc counter) total out-file))))

(defn screen [in-file out-file] 
  (let [input ((template :read) in-file)]
    (write-headers (first input) out-file)
    (try 
      (review (rest (rest input)) 1 (- (count input) 2) out-file)
      (catch Exception e (println "Review interrupted.")))))

(with-open [out-file (clojure.java.io/writer output)
            in-file (clojure.java.io/reader input)] 
  (print-exit-instructions)
  (repl/set-break-handler! (fn [sig] (print-exit-instructions)))
  (screen in-file out-file)
  (if (= template templates/acm) (shutdown-agents)))