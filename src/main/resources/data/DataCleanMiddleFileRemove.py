import os ;
import sys ;
import shutil ;

def getDirName():
    
    dirNameList = []
    
    curPathName = os.getcwd()

    fileNameList = os.listdir(curPathName)	

    for fileName in fileNameList:
        if os.path.isdir( fileName ):
	   dirNameList.append( curPathName + os.sep+fileName )

    return dirNameList

def removedGeneratedMiddleFiles():
    dirNameList = getDirName()

    '''
   
    traverse each data folder && 
    and find out not .xml files

    '''
    for filePath in dirNameList :
        fileSet = os.listdir( filePath )
	for fileName in fileSet :
	    fileName = filePath + os.sep+fileName 
	    if os.path.isdir( fileName ):
	       print 'remove middle generated dir ' + fileName
	       shutil.rmtree( fileName)
	    elif os.path.splitext(fileName)[1] != '.xml':	  
	       print 'remove middle generated file' + fileName
	       os.remove( fileName )


def getRemovedNameList():
    deleteFileNameList = []
    curPathName = os.getcwd()
    filelist = os.listdir( curPathName )
	  
    for  fileName  in filelist :
  	 if os.path.split(fileName)[0] != '.xml':
	       deleteFileNameList.append(curPathName+os.sep+fileName)
			   
    for fileName in deleteFileNameList:
         print( fileName )


removedGeneratedMiddleFiles()
