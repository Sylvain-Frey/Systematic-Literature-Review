;; Screener script for csv metadata for different input formats.
;; Supported formats: IEEE, Springer, self-defined default.

(ns screener)
(require '[clojure.data.csv :as csv])
(require '[clojure.java.io :as io])
(require '[clojure.string :as string])
(require '[clojure.repl :as repl])
(require '[net.cgrand.enlive-html :as html])

;; Parsing templates, define what column
;; contains relevant data in the input csv.

;;;;;;;;;;;;;;;;;;;
;; IEEE template
;;;;;;;;;;;;;;;;;;;
(def ieee-template {
  :separator \,
  :year 5
  :authors 1
  :title 0
  :abstract 10
  :url 15
  :parse (fn [line tag] (nth line (ieee-template tag)))
})


  
;;;;;;;;;;;;;;;;;;;;;
;; Springer template
;;;;;;;;;;;;;;;;;;;;;
(defn x-abstract-from-page [url]
  (let [page (slurp url)]
    (first (map html/text (html/select (html/html-snippet page) 
         [:html :body :div#wrapper :div#content 
          :div#kb-nav--main :div#kb-nav--main 
          :div.abstract-content.formatted :p.a-plus-plus])))))
(def springer-template {
  :separator \,
  :year 7
  :authors 6
  :title 0
  :abstract :special
  :url 8
  :parse (fn [line tag]
    (case tag
      :abstract (x-abstract-from-page (nth line (springer-template :url)))
      (nth line (springer-template tag))))
})


;;;;;;;;;;;;;;;;;;;
;; default template
;;;;;;;;;;;;;;;;;;;
(def default-template { 
  :separator \,
  :year 1
  :authors 2
  :title 3
  :abstract 4
  :url 7
  :parse (fn [line tag] (nth line (default-template tag)))
})


(def current-template 
  (case (nth *command-line-args* 2) 
    "ieee" ieee-template
    "springer" springer-template
    default-template))

(defn parse [tag line] 
  ((current-template :parse) line tag))

;; IO functions.

(def csv-input (nth *command-line-args* 0))
(def csv-output (nth *command-line-args* 1))

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

(defn print-exit-instructions []
  (println "Hit Ctrl-D to save and exit."))

(with-open [out-file (io/writer csv-output)
            in-file (io/reader csv-input)] 
  (print-exit-instructions)
  (repl/set-break-handler! (fn [sig] (print-exit-instructions)))
  (screen in-file out-file))