
Text Indexer and Feature Extractor
----------------------------------

@author Andreas Decker
        e0227283@student.tuwien.ac.at
        2012-04-15
        
        
ABGABESLOT 2 in TUWEL wurde genutzt!!!!

        
Usage: MyTIandFE.jar -i <input-dir> 	        set input root directory for data files
	                 -o <output-dir>            set output directory including (filename.arff)
		             -w <BOOL|TF|TFIDF|ALL>     set weighting option (ALL runs BOOL and TF and TFIDF)
		             -u <upperThreshold>        set upper threshold for document frequency
		             -l <lowerThreshold>        set lower threshold for document frequency
		             -noste <ON|OFF>            set stemming on or off
		             -stelang <ENGLISH|GERMAN>  set stemming language
		             -stecount <int>            set stemming iteration count
		             -nosto <ON|OFF>            set use of stopwords on or off
		             -stofil <stopfile>         set stopword file
		             -tmp <tem-dir>             set custom temporary directory


Default values:	
					input_dir = C:\IR\20_newsgroups_small
					output_file = C:\IR\myARFF.arff
					weighting = "BOOL
					upperThreshold = 0.70
					lowerThreshold = 0.005
					stemming = on
					stemming language = ENGLISH
					stemming count = 1
					stopwords = on
					stopword_file = .\ENG_Stopwords.txt
					temp_dir = .\Temp\
			
						            
Short documentation:
ANT:				use build build.xml to get jar file
JAR:	 			can be found in ./build, self-runnable need JRE6 an sufficient free RAM and HDD space
SRC:     			Project files are build in eclipse Indigo SR2
BuildUp: 			uses snowball, apache commons-io and jewelcli
Main Components:	extractFeatures()
						Reads all files from input dir tokenizes the text and stores it in a temp file
						Keeps dictionary in memory
					createIndex()
						Reads all fiels from tem dir and builds up arff file
						Uses dictionary
					scanForTokens(String)
						Used by extractFeatures()
						Scans Text for word tokens
						Builds up dictionary
						Process stopwords
						Does the stemming
