# StackExchange Recommender System
---
## Description
This project aims to create a simple recommender system based on machine learning algorithm {} provided by [Spark's mllib]() . 

## Dateset Download
+ You can download the dateset(both tesing , training) from StackExchange [DB dump-file website]() or you can also download the dataset used for this project by visiting [Baidu-Yun](http://pan.baidu.com/s/1i3Zeuup).


+ You can get the data file's description info by [clicking this link](https://ia600500.us.archive.org/22/items/stackexchange/readme.txt). 

+ After you getting the dataset unzip it under the path like this :<pre>
../StageExchangeRecommender/src/main/resources/data/unix.stackexchange.com 
</pre>


+ And do not forget to remove the .zip file from the path, in case of python script goes wrong 

## File Path Description
+ Here is the tree path of the whole project
+ <pre>../logs/  			
    |-error/    # only error info log files
    |-info/     # debug info log files
                # log level setting written in log4j.properties
</pre> 

+ <pre>
../src/
	|-main/
		|-java/
		|-resources/
			 |-data/{DataCleanMiddleFileRemove.py ,
     	       		DataCleanScript.py , stop_words_set.py} 

     		 |-log4j.properties
        	         |-sample_svm_data.txt
		
	         |-scala/
</pre>

+ <pre>
../pom.xml
../README.md
</pre> 

## Execute Steps
### Step One : clean raw data by running script in python

+ Python script under ./data/DataCleanScript.py is used to execute data cleaning job in which extracting and tokenizing question tags from Tags.xml and Posts.xml files to Tags.csv ,tags_filtered.txt and Questions_*.xml files. 
+ You can execute DataCleanScript.py file by running the commands below:<pre>
  $ python DataCleanScript.py
</pre>  

+ After executing the script a new file folder with name of cleanedData and under the new created folder: {Tags.csv , tags_filtered.txt and Questions_{0..}.xml} these middle files will be genderated.   

### Step Two : create index for Questions_*.xml by lucene written in java 
+ 