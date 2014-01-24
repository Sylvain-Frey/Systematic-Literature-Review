(ns solar.templates
  [:require [net.cgrand.enlive-html :as html]])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IEEE template for CSV files
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def ieee {
  :separator \,
  :year 5
  :authors 1
  :title 0
  :abstract 10
  :url 15
  :parse (fn [line tag] (nth line (ieee tag)))
})
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Springer template for CSV files
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn x-abstract-from-page [url]
  (let [page (slurp url)]
    (first (map html/text (html/select (html/html-snippet page) 
         [:html :body :div#wrapper :div#content 
          :div#kb-nav--main :div#kb-nav--main 
          :div.abstract-content.formatted :p.a-plus-plus])))))
(def springer {
  :separator \,
  :year 7
  :authors 6
  :title 0
  :abstract :special
  :url 8
  :parse (fn [line tag]
    (case tag
      :abstract (x-abstract-from-page (nth line (springer :url)))
      (nth line (springer tag))))
})

;;;;;;;;;;;;;;;;;;;;;;;
;; default CSV template
;;;;;;;;;;;;;;;;;;;;;;;
(def default { 
  :separator \,
  :id 0
  :year 1
  :authors 2
  :title 3
  :abstract 4
  :library 5
  :search-term 6
  :url 7
  :parse (fn [line tag] (nth line (default tag)))
})

(def default-headers ["id" "year" "author" "title" "abstract" "library" "search_term" "url"])
