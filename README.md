Systematic-Literature-Review
============================

Data extration and processing tools for a Systematic Literature Review. The general process assumed here consists in aggregating extracted data (author, year, title, abstract) into an csv file and screen the csv.


ACM parser
----------

ACM online library does not like data crawlers. You will have to run your queries manually and save the result html pages (maximum 20 papers per page). You can concatenate all pages together. Then, acm-parser will generate csv from the html, using:

java -cp lib/data.csv-0.1.2.jar:lib/enlive-1.1.1.jar:lib/tagsoup-1.2.1.jar:lib/clojure-1.5.1.jar clojure.main src/parser.clj samples/acm-page.html samples/acm-out.csv "query_content"


Screener
--------

This little tool helps with the screening process: given a csv file filled with paper metadata, screener goes through each paper, shows its title and abstract, prompts for a screening status ("Accepted", "Rejected" or "Borderline") and additional comments, and generates an updated csv file. Sample use:

java -cp lib/clojure-1.5.1.jar:lib/data.csv-0.1.2.jar clojure.main src/screener.clj samples/screen-test.csv samples/screen-result.csv
