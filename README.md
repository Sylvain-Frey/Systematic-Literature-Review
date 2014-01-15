Systematic-Literature-Review
============================

Data extration and processing tools for a Systematic Literature Review. The general process assumed here consists in aggregating extracted metadata (author, year, title, abstract) into a csv file and screen it (accept or reject papers).


ACM parser
----------

This script extracts metadata from offline ACM pages. You will have to run your queries manually and save the resulting html on disk (maximum 20 papers per page). You can concatenate all pages together. Then, acm-parser will generate csv from the html. Sample use:

java -cp "lib/*" clojure.main src/acm-parser.clj samples/acm.clojure.html samples/acm.clojure.csv "clojure"
    

Screener
--------

This script helps with the screening process: given a csv file filled with paper metadata, screener goes through each paper, shows its year, authors, title and abstract, prompts for a screening status ("Accepted", "Rejected" or "Borderline") and additional comments, and generates an updated csv file.

Screening csv files provided by IEEE Xplore:

java -cp "lib/*" clojure.main src/screener.clj samples/ieee.clojure.csv samples/ieee.clojure.screened.csv "ieee"
    
    

Screening csv files provided by Springer:
    
java -cp "lib/*" clojure.main src/screener.clj samples/springer.clojure.csv samples/springer.sclojure.screened.csv "springer" 


Screening csv files following a default template (such as the ones created by the acm parser above):
 
java -cp "lib/*" clojure.main src/screener.clj samples/acm.clojure.csv samples/acm.clojure.screened.csv "default"

