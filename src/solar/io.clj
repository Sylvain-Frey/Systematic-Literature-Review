;; Utility I/O and command line functions.
(ns solar.io
  [:require [solar.templates :as templates]]
  [:require [clojure.data.csv :as csv]])

;; Define several vars with several names and values.
(defn defs [names values]
  (doseq [[name value] (zipmap names values)]
    (intern *ns* (symbol name) value)))

;; Check correct arguments have been provided 
;; by the command line and map them to clojure definitions
;; or print an error message and abort execution.
(defn check-args [args error-message]
  (if (= (count *command-line-args*) (count args))
    (defs args *command-line-args*)
    (do 
      (println error-message "\n")
      (System/exit 0))))


;; Write a new CSV line to a CSV file.
(defn write-csv [file output]
  (csv/write-csv file [output]))

;; Write default template headers to a CSV file.
(defn write-default-headers [out-file]
  (write-csv out-file templates/default-headers))
