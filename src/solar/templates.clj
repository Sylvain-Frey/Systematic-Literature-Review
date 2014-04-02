;; Templates for parsing different file formats.

(ns solar.templates
  [:require [net.cgrand.enlive-html :as html]]
  [:require [clojure.string :as string]]
  [:require [clojure.java.shell :as shell]]
  [:require [clojure.data.csv :as csv]])


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

  :read (fn [file] (csv/read-csv file :separator (ieee :separator)))
  :parse (fn [tag line] (nth line (ieee tag)))
  :generate-id-from (fn [line]
                      (string/join "-" [
                        (.substring ((ieee :parse) :year line) 2 4)
                        (last (string/split (first (string/split ((ieee :parse) :authors line) #",")) #" "))
                        (first (string/split ((ieee :parse) :title line) #" "))
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

  :read (fn [file] (csv/read-csv file :separator (springer :separator)))
  :parse (fn [tag line]
    (case tag
      :abstract (x-abstract-from-page (nth line (springer :url)))
      (nth line (springer tag))))

  :generate-id-from (fn [line]
                      (string/join "-" [
                        (.substring ((springer :parse) :year line) 2 4)
                        (last (string/split (first (string/split ((springer :parse) :authors line) #",")) #" "))
                        (first (string/split ((springer :parse) :title line) #" "))
                        ]))
})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ACM template for stealing abstracts
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; example URLs:
;; pdf http://dl.acm.org/ft_gateway.cfm?id=2398803&type=pdf&coll=DL&dl=ACM&CFID=286755835&CFTOKEN=86480529
;; cit http://dl.acm.org/citation.cfm?id=2398776.2398803&coll=DL&dl=ACM&CFID=286755835&CFTOKEN=86480529
;; abs http://dl.acm.org/tab_abstract.cfm?id=2398803&usebody=tabbody&cfid=404509037&cftoken=96850275
;; get-page deduces the cit url form the pdf url 
;; prefix DDD. in the id relates to a collection and can be omitted.

(defn get-page [url]
  (let [correct-url 
        (str 
         (string/replace 
          (string/replace url "&type=pdf" "") 
          "ft_gateway.cfm" "citation.cfm") 
         "&preflayout=flat")]
  (:out (shell/sh "elinks" "--dump" correct-url))))

(defn x-abstract-from-acm [url]
  (let [page (get-page url)]
    (re-find #"ABSTRACT[^\[]*\[" (string/replace page "\n" ""))))

(def acm {

  :separator \,
  :id 0
  :year 1
  :authors 2
  :title 3
  :abstract 4
  :library 5
  :search-term 6
  :url 7

  :read (fn [file] (csv/read-csv file :separator (acm :separator)))
  :parse (fn [tag line]
    (case tag
      :abstract (x-abstract-from-acm (nth line (acm :url)))
      (nth line (acm tag))))

  :generate-id-from (fn [line]
                      (string/join "-" [
                        (.substring ((acm :parse) :year line) 2 4)
                        (last (string/split (first (string/split ((acm :parse) :authors line) #",")) #" "))
                        (first (string/split ((acm :parse) :title line) #" "))
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

  :read (fn [file] (csv/read-csv file :separator (isiwos :separator)))
  :parse (fn [tag line] (nth line (isiwos tag)))
  :generate-id-from (fn [line]
                      (string/join "-" [
                        (.substring ((isiwos :parse) :year line) 2 4)
                        (first (string/split ((isiwos :parse) :authors line) #","))
                        (first (string/split ((isiwos :parse) :title line) #" "))
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

  :read (fn [file] (csv/read-csv file :separator (default :separator)))
  :parse (fn [tag line] (nth line (default tag)))
  :generate-id-from (fn [line]
                      (string/join "-" [
                        (.substring ((default :parse) :year line) 2 4)
                        (last (string/split (first (string/split ((default :parse) :authors line) #",")) #" "))
                        (first (string/split ((default :parse) :title line) #" "))
                        ]))
})

(def default-headers ["id" "year" "author" "title" "abstract" "library" "search_term" "url"])





;; Map templates names to actual templates.

(defn for-name [template-name]
  (case template-name
    "ieee" ieee
    "springer" springer
    "isiwos" isiwos
    default))