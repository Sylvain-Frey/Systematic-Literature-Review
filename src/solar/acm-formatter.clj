;; Formatting script extracting metadata from ACM HTML pages.
;; Output format: self-defined CSV.

(ns solar.acm-parser
  [:require [solar.io :as io]]
  [:require [clojure.java.io]]
  [:require [clojure.string :as string]]
  [:require [net.cgrand.enlive-html :as html]]
  [:require [solar.templates :as templates]]
  [:require [org.httpkit.client :as http]])
  
;; Check command line arguments.

(io/check-args
  ['input 'output 'search-term]
  "
  Usage: acm-formatter <input-file> <output-file> <query>

  Example: acm-formatter acm.html acm.formatted.csv my_query")


;; Data extraction functions.

(defn x-papers [acm-page]
  (html/select (html/html-snippet acm-page) 
               [:html :body :div :table :tbody 
                :tr :td :table :tbody 
                :tr :td :table :tbody 
                :tr :td :table :tbody]))

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

(defn x-abstract* [paper]
  (let [text (first (map html/text (html/select paper [:tr :td :div.abstract2])))]
    (if-not (string/blank? text) (purge text))))

(defn x-library [paper]
  (let [text (first (map html/text (html/select paper [:tr :td :div.addinfo])))]
    (if-not (string/blank? text) (purge text))))
    
(defn x-search-term [paper] search-term) ;; provided by user

(defn x-url [paper]
  (:href (:attrs (first (html/select paper 
                       [:tr :td :table :tbody 
                        :tr :td :table :tbody 
                        :tr :td :a])))))

(defn abstract-url [url]
  (let [id (re-find #"id=\d+" url)]
    (str "http://dl.acm.org/tab_abstract.cfm?" id)))

(defn x-abstract [paper]
  (let [url (x-url paper)]
    (if (string/blank? url)
      (x-abstract* paper)
      (let [abstract (http/get (abstract-url url))]
        (Thread/sleep 2000) ;; shhhh... ACM crawl filter could hear us...
        (purge
         (reduce str "" 
           (map html/text (html/html-snippet (:body @abstract)))))))))

    
(defn x-id [paper] ;; id = year - author - first (significant? :s) word in title
  (string/join "-" [
    (.substring (x-year paper) 2 4)
    (last (string/split (first (string/split (x-author paper) #",")) #" "))
    (first (string/split (x-title paper) #" "))]))

;; csv layout convention:
;; id, year, author, title, abstract, library, search_term, url
  
(def handlers [x-id x-year x-author x-title x-abstract x-library x-search-term x-url])



;; Main functions.
  
(defn extract [in-file out-file]
  (doseq [paper (x-papers in-file)]
    ;; if author empty, it's a false match of x-papers, discard it
    (let [author-text (x-author paper)]
      (if-not (string/blank? author-text)
        (io/write-csv out-file 
          (map #(% paper) handlers))))))

(let [in-file (slurp input)]
  (with-open [out-file (clojure.java.io/writer output)]
    (io/write-default-headers out-file)
    (extract in-file out-file)))
    
