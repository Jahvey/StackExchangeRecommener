package org.kylin.zhang.StackExchange.indexer.service;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.kylin.zhang.StackExchange.indexer.model.QuestionBean;
import org.kylin.zhang.StackExchange.indexer.util.QuestionXmlParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Aimer on 2016/2/6.
 */
public class QuestionIndexer {
    public static String indexFolderName ="indexResults" ;
    private IndexWriter questionIndexWriter ;
    private Directory  questionIndexDirectory ;
    private IndexWriterConfig questionIndexConfig ;
    private String indexDirectory ;
    private static QuestionIndexer questionIndexer = null ;

    private static Logger logger = Logger.getLogger(QuestionIndexer.class) ;

    public QuestionIndexer (String indexDirectory){
        try{
            this.indexDirectory = indexDirectory ; // ../data/***(data set folder name)/
            this.questionIndexWriter = getIndexWriter() ;
        } catch (IOException exception){
            logger.error("something wrong during get question index writer");
            exception.printStackTrace();
        }
    }

    public static QuestionIndexer getInstance(String indexPath ){
        if ( questionIndexer == null )questionIndexer = new QuestionIndexer(indexPath) ;
        return questionIndexer ;
    }
    private IndexWriter getIndexWriter() throws IOException {
        if ( this.questionIndexWriter == null ){
            Analyzer analyzer = new StackExChangeAnalyzer () ;
            questionIndexConfig = new IndexWriterConfig(Version.LUCENE_47 , analyzer) ;
            File f = new File ( this.indexDirectory + File.separator + indexFolderName) ;

            if ( f.exists()){
                f.delete() ;
            }
            f.mkdirs() ;

            questionIndexDirectory = FSDirectory.open( new File( this.indexDirectory)) ;
            questionIndexWriter = new IndexWriter(questionIndexDirectory, questionIndexConfig) ;
        }
        return questionIndexWriter ;
    }

    public void indexQuestions(List<QuestionBean> questionBeanList)  throws  IOException {
        Field field = null ;
        for (QuestionBean bean : questionBeanList){
            Document newDoc = new Document () ;

            // Adding question Id into Field
            field = new IntField("qId", bean.getQuestionId(), Field.Store.YES) ;
            newDoc.add(field) ;

            // Adding question content as --- qContent field
            field = new TextField("qContent", bean.getQuestionContent(), Field.Store.YES) ;
            newDoc.add(field);

            // Add question tags as --- qTag
            // tags are stores as Set of String
            int i = 1 ;
            for ( String tag : bean.getQuestionTags()){
                field = new StringField("qTag"+i , tag.toLowerCase(), Field.Store.YES) ;
                i++ ;
                newDoc.add(field);
            }

            // Add the current question into index
            questionIndexWriter.addDocument( newDoc);
        }
    }

    public void close(){
        try{
            this.questionIndexWriter.close();
        }catch(Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main (String [] args) throws Exception {
        File f ;
        FileInputStream fileInputStream ;
        QuestionIndexer questionIndexer = null ;
        QuestionXmlParser questionXmlParser = new QuestionXmlParser() ;
        List<QuestionBean> qBeansList = null ;
        List<String> questilFolderList = questionXmlParser.getQuestionDataSetFolderList() ;


         /* 1. create folder to store index */
        for ( String folderName : questilFolderList){
            // 1. create result index folder path name

            String questionFilePath = folderName + File.separator + QuestionXmlParser.questionFolderName ;
            String indexResultPath =  folderName + File.separator + QuestionIndexer.indexFolderName ;

           logger.debug("question file folder " + questionFilePath); ;
            logger.debug("index result folder " + indexResultPath); ;

            f = new File ( indexResultPath ) ;
            if ( f.exists() )
                f.delete() ;
            f.mkdirs() ;

            // 2. extract Question-Bean from Question * .xml files
            qBeansList = questionXmlParser.run(questionFilePath) ;

            logger.debug("extract " + qBeansList.size() + " beans from folder  " + questionFilePath); ;
            // 3. write each Question-Bean into index file under result index folder
            System.out.println("indexing file " + indexResultPath) ;
            questionIndexer = new QuestionIndexer(indexResultPath) ;
            questionIndexer.indexQuestions(qBeansList);
            questionIndexer.close();

            System.out.println("parsing beans : "+ qBeansList.size()) ;


          /*  System.out.println( indexPath ) ;
            f = new File ( indexPath  ) ;

            f.mkdirs() ;

            // 2. extract QuestionBeans from Question xml files
            qBeansList = questionXmlParser.run(folderName+File.separator+QuestionXmlParser.questionFolderName) ;
            System.out.println( qBeansList.size()) ;

            // 3. write QuestionBeans' index under the result index folder
            questionIndexer = new QuestionIndexer(indexPath) ;
            questionIndexer.indexQuestions( qBeansList);
            questionIndexer.close();*/
        }


    }
}
