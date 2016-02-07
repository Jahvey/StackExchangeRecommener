from bs4 	    import BeautifulSoup
from docopt 	import docopt
import xml.etree.ElementTree as ET
import csv ;
import os  ;
import re  ;
import sys ;
import threading ;
import time ;
from   time import sleep , ctime 


def now():
    return str(time.strftime( '%Y-%m-%d %H:%M:%S', time.localtime()))


def getDirNameList():
    dirNameList = []
    curPathName = os.getcwd()
    dirList = os.listdir( curPathName )

    for dirName in dirList:
        if os.path.isdir( dirName ):
           dirNameList.append( curPathName + os.sep + dirName )

    return dirNameList

'''
class threadDataCleaner(threading.Thread):
      
	def __init__(self,fileDir):
	    super(threadDataCleaner, self).__init__()
	    self.dataDirName = fileDir
	
	def run(self):
	    dataCleaner = DataCleanModel(self.dataDirName)
	    dataCleaner.run()
'''

class DataCleanModel:

	def __init__(self,AB_Path):
	    self.working_path =   AB_Path     # AB is short for absolute path
	    self.count_limiter =  '150'       # this variable is used to limit the minimum counter number
 	    self.max_question_limiter = '300'

	    self.tag_xml_file = 'Tags.xml'
	    self.tag_csv_file = 'Tags.csv'
	    self.tag_txt_filter_file = 'tags_filtered.txt'
	    self.post_xml_file = 'Posts.xml'
	    self.cleand_question_file_prefix = 'Questions_'
	    self.cleand_question_file_extension = '.xml'
	    self.cleaned_data_file_folder_name = 'cleanedData'

	def setup(self):
	    # in setup method , we change our directory into the working path and if the clean data folder does not exists we create it
	    if not os.path.exists(self.working_path):
	       print 'working_path: %s not exists'%(self.working_path)
               return

	    os.chdir(self.working_path)

	    if not os.path.exists(self.cleaned_data_file_folder_name):
	       os.mkdir(self.cleaned_data_file_folder_name)


	def xmlTcsv(self):
	    xmlTagInputFile  = open(self.tag_xml_file, 'rb')
	    csvTagOutputFile = open(self.tag_csv_file, 'wb')
	    soup = BeautifulSoup(xmlTagInputFile, 'lxml-xml')
	    xmlTagInputFile.close()

	    rowSet = soup.find_all('row')

	    for line in rowSet:
	        csvTagOutputFile.write(line.get('TagName')+','+line.get('Count')+'\n')

	    csvTagOutputFile.close()

	def csv_to_filtered(self):
		csvFile = open(self.tag_csv_file,'rb')
		tagFile = open(self.tag_txt_filter_file,'wb')
		count = 0

		tags = csv.reader(csvFile, delimiter=',')


		for row in tags:
		    if int(row[1]) > int(self.count_limiter):
			   count +=1
			   # print 'here is the tag count > %s %s'%(self.count_limiter,row)
			   tagFile.write(row[0]+'\n')
		tagFile.close()
		csvFile.close()
		print count

	def posts_to_question(self):
		tagFile = open(self.tag_txt_filter_file)
		tagSet  = set()

		for tag in tagFile:
		    tagSet.add(tag.strip())

                post_file = open(self.post_xml_file,'rb')
		post_soup = BeautifulSoup(post_file,'lxml-xml')
		postType_1_Set = post_soup.find_all("row", PostTypeId="1")
		# here we extract all rows which contain PostTypeId="1" which means this is the record of question

		totalFileCounter = 0
		questionCounter  = 0
		folder_path =self.working_path+'/'+self.cleaned_data_file_folder_name


		newXmlFile = open(folder_path+'/'+self.cleand_question_file_prefix+str(totalFileCounter)+self.cleand_question_file_extension,'wb')
		newXmlFile.write('<questions>\n')
		isQuestionValid = False     # boolean used to justify whether the question's tag set is a sub-set of filter-tags-set

		for line in postType_1_Set:
			questionId = line.get('Id')

			questionTitle = line.get('Title')
			cleanedQTitle,replacedCounter = re.subn("\<(([A-Za-z]*)|(\/[A-Za-z]*))\>","",questionTitle)

			questionBody  = line.get('Body')
			cleanedQBody,replacedCounter  = re.subn("&#10;|&#xD;|&#xA;|\\n|\\r","",questionBody)

			cleanedQBody,replacedCounter = re.subn("\<([^&]*?)\>","", cleanedQBody)
			cleanedQBody,replacedCounter = re.subn("&gt;|&lt;|([\/]?)","", cleanedQBody)

			questionAnsCount  = line.get('AnswerCount')
			questionViewCount = line.get('ViewCount')
			questionTags      = line.get('Tags')

			if questionTags is None:
			   print '[error] : no tags in this line'
			   continue

			cleanedQTags,replacedCounter = re.subn("&lt;|<","", questionTags)
			cleanedQTags,replacedCounter = re.subn("&gt;|>",",",cleanedQTags)

			questionTagSet = set(cleanedQTags.split(','))
			questionTagSet.remove('')

			if questionTagSet.issubset(tagSet):
			   	validQuestion = True
				# here we set the extracted attributed above from local variables into tags of the output XML tree
				root = ET.Element('question')

				qId = ET.SubElement(root, 'Id')
				qId.text = questionId

				qTitle = ET.SubElement(root,'Title')
				qTitle.text = cleanedQTitle

				qBody = ET.SubElement(root,'Tags')
				qBody.text = cleanedQTags

				qAnsCount = ET.SubElement(root,'AnsCount')
				qAnsCount.text = questionAnsCount

				qViewCount = ET.SubElement(root,'ViewCount')
				qViewCount.text = questionViewCount

				uncleanData = ET.tostring(root, encoding='utf-8')
				cleanData,replaceCounter = re.subn("&#10|&#xA|\\n|\\r"," ", uncleanData)

				if validQuestion : # which means the tagSet is a subset of the tag-set we filtered from tags-file
     			           newXmlFile.write(cleanData)
				   newXmlFile.write('\n')
				   questionCounter += 1
				   validQuestion = False

			        if questionCounter >= self.max_question_limiter:
				   # if number of the lines in current xml file is larger than maximum limit, we create a new file
				   newXmlFile.write('</questions>')
				   newXmlFile.close()
				   print ' File number ' + str(totalFileCounter) +' done with %d lines in it'%(questionCounter)
				   questionCounter = 0
				   totalFileCounter +=1

				   newXmlFile = open(folder_path+'/'+self.cleand_question_file_prefix+str(totalFileCounter)+self.cleand_question_file_extension,'wb')
				   newXmlFile.write('<questions>')

		newXmlFile.write('</questions>')
		newXmlFile.close()
		print ' File number ' + str(totalFileCounter) +' done !!!'


	def run(self):
	    self.setup()

	    # fist step , we change Tags.xml file into Tags.csv file
	    self.xmlTcsv()

	    # sencond step , filter tags in Tags.csv file , we got tags_filtered.txt
	    self.csv_to_filtered()

		# finally step, we extract tags from tags_filtered.txt to build a tag-set
		# then we extract each lines from Posts.xml , create another tag-set
		# if the Posts.xml's tag-set is sub-set of the tag-set we create from tags_filtered.txt
		# we write the corresponding data in Posts.xml into the new .xml file
	    self.posts_to_question()
		
'''	
def now():
    return str(time.strftime( '%Y-%m-%d %H:%M:%S', time.localtime())


def getDirNameList():
    dirNameList = []
    
    curPathName = os.getcwd()
    dirList = os.listdir( curPathName )

    for dirName in dirList:
        if os.path.isdir( dirName ):
	   dirNameList.append( curPathName + os.sep + dirName )
	
    return dirNameList
'''		
def main():

    threadPool = []
	
    dirNameList = getDirNameList ()
	
    print 'start time '+now()	
    for dirName in dirNameList :
        dataCleaner =  DataCleanModel(dirName)
	dataCleaner.run()
			
    print 'end time '+now()


if __name__ == '__main__':
   sys.exit(main())


