package org.kylin.zhang.StackExchange.indexer.util;

import org.apache.log4j.Logger;
import org.kylin.zhang.StackExchange.indexer.model.QuestionBean;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by Aimer on 2016/2/5.
 */

class QuestionXmlParserHandler extends DefaultHandler{
    private Logger logger = Logger.getLogger(QuestionXmlParser.class) ;
    private Stack<QuestionBean> qBeansStack ;
    private Stack<String>   qElementsStack ;
    private List<QuestionBean> qBeanList ;

    public QuestionXmlParserHandler(){
        this.qBeanList = new ArrayList<QuestionBean>() ;
        this.qBeansStack = new Stack<QuestionBean>() ;
        this.qElementsStack = new Stack<String>() ;
    }

    @Override
    public void startDocument() throws SAXException {
        // System.out.println("---------------- begin parsing xml ---------------------") ;
        logger.debug("----------------------begin parsing xml ---------------------");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.qElementsStack.push(qName) ;

        if("question".equals(qName)) {
            QuestionBean qBean = new QuestionBean() ;
            this.qBeansStack.push(qBean) ;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        /**
         * here is the question xml file 's structure
         * <question>\
         *      <Id>6</Id>
         *      <Title>What are your favorite command line features or tricks ?</Title>
         *      <Tags>shell,command-line,</Tags>
         *      <ViewCount>39458</ViewCount>
         * </question>
         * */

        String currentStr = new String(ch, start , length ).trim() ;
       // System.out.println("current string " + currentStr) ;

        /* find out current element string belong which kind of xml attribute */

        if ("Id".equals(this.qElementsStack.peek())){
            QuestionBean qBean = this.qBeansStack.peek() ;
            qBean.setQuestionId( Integer.parseInt(currentStr));
        }
        else if ("Title".equals(this.qElementsStack.peek())){
            QuestionBean qBean = this.qBeansStack.peek() ;
            qBean.setQuestionTitle(currentStr);
        }
        else if("Body".equals(this.qElementsStack.peek())){
            QuestionBean qBean = this.qBeansStack.peek() ;
            qBean.setQuestionBody(currentStr);
        }
        else if ("Tags".equals(this.qElementsStack.peek())) {
            //  Question xml file's Tags' attribute contain multi values
            QuestionBean qBean = this.qBeansStack.peek() ;
            Set<String> tagSet = new HashSet<String>() ;
            String [] tagsArray = currentStr.split(",") ; // transfer tags string (separated by comma) into String array

            for (String tag : tagsArray){
                if ( !tag.isEmpty()){
                    tagSet.add(tag) ;
                }
            }
            // finally we insert tagSet into QuestionBean's tag member
            qBean.setQuestionTags(tagSet);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
       this.qElementsStack.pop() ;

        if ("question".equals(qName)){
            QuestionBean qBean = this.qBeansStack.pop() ;
            this.qBeanList.add(qBean);
        }
    }

    // extracted QuestionBeanList getter
    List<QuestionBean> getqBeanList(){
        return this.qBeanList ;
    }

    @Override
    public void endDocument() throws SAXException {
    //    System.out.println("---------------- finish parsing xml ---------------------") ;
        logger.debug("----------------------begin parsing xml ---------------------");
    }
}

public class QuestionXmlParser{

    public static String dataSetPathName = "/data" ;
    public static String questionFolderName ="cleanedData" ;
    private List<String> questionDataSetFolderList  ;
    private static Logger logger = Logger.getLogger(QuestionXmlParser.class) ;

    public QuestionXmlParser(){
        try{

            URL url = QuestionXmlParser.class.getResource(dataSetPathName) ;

            if ( url == null ){
                System.out.println("failed to locate dataset file path") ;
                logger.error("failed to locate dataset file path");
                return ;
            }

            String [] dataSetFolderArray = null ;

            dataSetFolderArray  = ( new File(url.getPath()).list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    boolean flag = false;

                    if (  !name.endsWith(".py") && !name.endsWith(".txt") ){
                        flag = true ;
                    }
                    return flag ;
                }
            }) ) ;

            if ( dataSetFolderArray.length <= 0 ){
                System.out.println("data set folder array is empty") ;
                logger.error("data set folder array is empty");
                return ;
            }
            this.questionDataSetFolderList = new ArrayList<String>() ;


            for ( String dataPath : dataSetFolderArray){
                File f  = new File( url.getPath()+File.separator+dataPath) ;
                this.questionDataSetFolderList.add( f.getAbsolutePath() );
            }

        } catch(Exception e){
            logger.error("something wrong happen when extracting question file path");
            e.printStackTrace();
        }
    }
    public List<QuestionBean> parseXml(InputStream inputStream){

        List<QuestionBean> qBeanList = new ArrayList<QuestionBean>() ;

        try{
            QuestionXmlParserHandler parserHandler = new QuestionXmlParserHandler () ;

            XMLReader parser = XMLReaderFactory.createXMLReader() ;

            parser.setContentHandler(parserHandler);

            InputSource source = new InputSource(inputStream) ;

            parser.parse(source);

            qBeanList = parserHandler.getqBeanList() ;

        } catch(SAXException e){
            e.printStackTrace();
            logger.error("Failed to parse the Question xml data file SAX error");

        } catch (IOException e){
            e.printStackTrace();
            logger.error("Failed to parse the Question xml data file cause IOException ");
        }

        if ( qBeanList == null || qBeanList.size() <= 0 ){
            System.out.println("oh low ! the qBean list is null ") ;
            logger.error("oh low ! Question Beans List is null ! ");
        }
        return qBeanList ;
    }

    public  List<String> getQuestionDataSetFolderList(){
        return this.questionDataSetFolderList ;
    }

    public List<QuestionBean>  run(String questionFolderPath ) {
        FileInputStream fileInputStream;
        List<QuestionBean> questionBeans;

        File[] fileArray = (new File(questionFolderPath).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getAbsolutePath().endsWith(".xml"))
                    return true;
                else
                    return false;
            }
        }));

        if (fileArray == null ) {
            System.out.println("can not find Question*.xml file under path " + questionFolderPath);
            logger.error("can not find Question*.xml file under path " + questionFolderPath);
            return null;
        }

        questionBeans  = new ArrayList<QuestionBean>() ;

        for (File file : fileArray) {
            List<QuestionBean> qBean = null ;
            try {
                fileInputStream = new FileInputStream(file);
                 qBean = parseXml(fileInputStream) ;
            } catch (Exception e) {
                logger.error("something wrong happen during reading file " + file.getAbsolutePath());
                e.printStackTrace();
            }

            if ( qBean!= null && qBean.size() > 0){
                questionBeans.addAll(qBean) ;
            }
            else{
                System.out.println("could not extract Question Beans from file "+ file.getAbsolutePath()) ;
            }
        }
        return questionBeans ;
    }

    public static void main (String [] args ) throws Exception {

        URL url = QuestionXmlParser.class.getResource("/data") ;
        if ( url != null )
        System.out.println(url.getPath()) ;

    /*    File f ;
        FileInputStream fileInputStream ;
        QuestionXmlParser questionXmlParser = new QuestionXmlParser() ;
        List<QuestionBean> qBeansList = null ;
        List<String> questilFolderList = questionXmlParser.getQuestionDataSetFolderList() ;

        for ( String folderName : questilFolderList){
            System.out.println( folderName ) ;

            List<QuestionBean> tempQBean = questionXmlParser.run(folderName+File.separator + QuestionXmlParser.questionFolderName) ;

            if ( qBeansList == null ){
                qBeansList = tempQBean ;
            } else{
                qBeansList.addAll(tempQBean) ;
            }
            System.out.println(qBeansList.size()) ;

            System.out.println(qBeansList.get(1)) ;
        }*/


}
}
