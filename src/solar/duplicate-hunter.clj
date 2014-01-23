;; Parse a CSV file following the "default" format,
;; and look for duplicate paper IDs.
;; Warning: due to different - and sometimes buggy (Springer) -
;; author formattings, the duplicate hunt may fail.


(ns solar.duplicate-hunter
  [:require [clojure.java.io :as io]]
  [:require [clojure.string :as string]]
  [:require [clojure.data.csv :as csv]]
  [:require [solar.templates :as templates]])

(def input (nth *command-line-args* 0))

(defn read-input [file] 
  (csv/read-csv file :separator \,))

(defn hunt [input ids]
  (let [line (first input)]
    (if line
      (let [id (nth line 0)] 
        (if (contains? ids id)
          (recur (next input) (assoc ids id (+ (get ids id) 1)))
          (recur (next input) (assoc ids id 1))))
      ids)))

(with-open [in-file (io/reader input)]
  (let [input (read-input in-file)]
    (let [counts (hunt (rest input) (hash-map))]
      (println (filter #(> (second %) 1) counts)))))