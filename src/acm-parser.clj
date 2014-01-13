(ns acm_parser)
(require '[clojure.java.io :as io])
(require '[clojure.string :as string])
(require '[net.cgrand.enlive-html :as html])
(require '[clojure.data.csv :as csv])

(def src-html (nth *command-line-args* 0))
(def dest-csv (nth *command-line-args* 1))
(def search-term (nth *command-line-args* 2))

(defn read-input [file-name]
  (slurp file-name))

(defn write-output [file output]
  (csv/write-csv file [output]))
  
  
;; data extraction functions

(defn x-papers [acm_page]
  (html/select (html/html-snippet acm_page) [:html :body :div :table :tbody :tr :td :table :tbody :tr :td :table :tbody :tr :td :table :tbody]))

(defn purge [text]
  (string/join "" (string/trim (string/replace text #"[\t\n]" ""))))

(defn year-matcher [text] (re-matcher #"\d\d\d\d" text))

(defn x-year [paper]
  (let [date-text (first (map html/text (html/select paper [:tr :td.small-text])))]
    (if-not (string/blank? date-text) (re-find (year-matcher date-text)))))
  
(defn x-author [paper]
  (let [authors (map html/text (html/select paper [:tr :td :div.authors :a]))]
    (string/join ", " authors)))

(defn x-title [paper]
  (let [text (first (map html/text (html/select paper [:tr :td :a.medium-text])))]
    (if-not (string/blank? text) (purge text))))

(defn x-abstract [paper]
  (let [text (first (map html/text (html/select paper [:tr :td :div.abstract2])))]
    (if-not (string/blank? text) (purge text))))

(defn x-library [paper]
  (let [text (first (map html/text (html/select paper [:tr :td :div.addinfo])))]
    (if-not (string/blank? text) (purge text))))
    
(defn x-search-term [paper] search-term) ;; provided by user

(defn x-url [paper]
  (:href (:attrs (first (html/select paper [:tr :td :table :tbody :tr :td :table :tbody :tr :td :a])))))
    
(defn x-id [paper] ;; id = year - author - first (significant? :s) word in title
  (string/join "-" [
    (.substring (x-year paper) 2 4)
    (last (string/split (first (string/split (x-author paper) #",")) #" "))
    (first (string/split (x-title paper) #" "))
  ]))

;; csv layout convention:
;; id, year, author, title, abstract, library, search_term, url
  
(def headers ["id" "year" "author" "title" "abstract" "library" "search_term" "url"])

(def handlers [x-id x-year x-author x-title x-abstract x-library x-search-term x-url])


;; main steps

(defn write-headers [out-file]
  (write-output out-file headers))
  
(defn extract [in-file out-file]
  (doseq [paper (x-papers in-file)]
    ;; if author empty, it's a false match of x-papers, discard it
    (let [author-text (x-author paper)]
      (if-not (string/blank? author-text)
        (write-output out-file 
          (map #(% paper) handlers))))))

(let [in-file (read-input src-html)]
  (with-open [out-file (io/writer dest-csv)]
    (do
      (write-headers out-file)
      (extract in-file out-file))))
    