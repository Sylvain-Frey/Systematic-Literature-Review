Systematic-Literature-Review
============================

Data extraction and processing tools for a Systematic Literature Review:
- extract paper metadata from HTML pages (ACM online library).
- format heterogeneous CSV metadata (Springer, IEEE, ISI Web of Science) to a common CSV template:
       "id, year, authors, title, abstract, library, search-term, url"
- screen metadata : review paper abstract, title... and filter irrelevant papers out.

This documentation shows some sample runs using data in the samples/ folder.


ACM formatter
-------------

This script extracts metadata from ACM pages:
- run search queries manually and save the resulting HTML on disk (maximum 20 papers per page).
- concatenate all pages together. 
- run acm-formatter to generate CSV from the HTML. 

Usage: acm-formatter "input-file" "output-file" "query"

Example:

    ./solar.sh src/solar/acm-formatter.clj samples/acm.clojure.html samples/acm.clojure.csv clojure



CSV formatter
-------------

Convert CSV files provided by online libraries (IEEE, Springer, ISI Web of Science) into a common CSV template:

    Template:   "id, year, authors, title, abstract, library, search-term, url"

Formatting also creates a unique ID per paper, namely "year"-"1st author"-"1st word in title", useful for detecting duplicates.

Usage: csv-formatter "input-file" "output-file" "format" "query"

Available input templates: ieee, springer, isiwos (cf. src/solar/templates.clj)

Examples:
    
    ./solar.sh src/solar/csv-formatter.clj samples/ieee.clojure.csv samples/ieee.clojure.formatted.csv ieee clojure
    ./solar.sh src/solar/csv-formatter.clj samples/springer.clojure.csv samples/springer.clojure.formatted.csv springer clojure

Formatting Springer CSV takes time since the script downloads paper abstracts from the online library. Be patient!



Duplicate filter
----------------

Filter duplicates from a metadata CSV file in standard format.

Example: duplicate-filter "input.csv" "output.csv"



Screener
--------

Given a CSV file filled with paper metadata, screener goes through each paper, shows its year, authors, title and abstract, prompts for a screening status ("Accepted", "Rejected" or "Borderline") and additional comments, and generates an updated CSV file.

Screener supports several CSV formats as input: IEEE, Springer, and custom.

Usage: screener "input-file" "output-file" "format"

Examples:

    ./solar.sh src/solar/screener.clj samples/acm.clojure.csv samples/acm.clojure.screened.csv default
    ./solar.sh src/solar/screener.clj samples/ieee.clojure.csv samples/ieee.clojure.screened.csv ieee
    ./solar.sh src/solar/screener.clj samples/springer.clojure.csv samples/springer.clojure.screened.csv springer
    ./solar.sh src/solar/screener.clj samples/springer.clojure.formatted.csv samples/springer.clojure.screened.csv default




Known issues
------------

Due to Springer weird template for author names, IDs generated for Springer papers may not match their duplicates.

ACM pages sometimes provide partial abstracts.

ISI Web of Science CSV files are not UTF-8: the formatter does not like it. Convert to UTF-8 before formatting.


Questions, support
------------------

Sylvain Frey, Lancaster University: s dot frey at lancaster dot ac dot uk.
