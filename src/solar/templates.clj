(ns solar.templates
  [:require [net.cgrand.enlive-html :as html]]
  [:require [clojure.string :as string]])


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
  :generate-id-from (fn [line]
                      (string/join "-" [
                        (.substring ((ieee :parse) line :year) 2 4)
                        (last (string/split (first (string/split ((ieee :parse) line :authors) #",")) #" "))
                        (first (string/split ((ieee :parse) line :title) #" "))
                        ]))
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

  :generate-id-from (fn [line]
                      (string/join "-" [
                        (.substring line 2 4)
                        (last (string/split (first (string/split ((springer :parse) line :authors) #",")) #" "))
                        (first (string/split ((springer :parse) line :title) #" "))
                        ]))
})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ISI Web of Science template for CSV files
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def isiwos {
  :separator \tab
  :year 31
  :authors 1
  :title 9
  :abstract 32
  :url 47
  :parse (fn [line tag] (nth line (isiwos tag)))

  :generate-id-from (fn [line]
                      (string/join "-" [
                        (.substring ((isiwos :parse) line :year) 2 4)
                        (first (string/split ((isiwos :parse) line :authors) #","))
                        (first (string/split ((isiwos :parse) line :title) #" "))
                        ]))
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

  :generate-id-from (fn [line]
                      (string/join "-" [
                        (.substring ((default :parse) :year line) 2 4)
                        (last (string/split (first (string/split ((default :parse) :authors line) #",")) #" "))
                        (first (string/split ((default :parse) :title line) #" "))
                        ]))
})

(def default-headers ["id" "year" "author" "title" "abstract" "library" "search_term" "url"])
