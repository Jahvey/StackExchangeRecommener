package org.kylin.zhang.StackExchange.indexer.driver;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.kylin.zhang.StackExchange.indexer.model.QuestionBean;
import org.kylin.zhang.StackExchange.indexer.service.QuestionIndexer;
import org.kylin.zhang.StackExchange.indexer.util.ConstantList;
import org.kylin.zhang.StackExchange.indexer.util.QuestionXmlParser;
import org.kylin.zhang.StackExchange.segment.QuestionSearcher;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aimer on 2016/2/10.
 */
public class Driver {
    public static void main (String [] args ){
/*      first setp ;

        URL url = Driver.class.getResource("/data/cstheory.stackexchange.com") ;
        System.out.println( url.getPath() ) ;

        //
        try{
            indexQuestionFiles( new File( url.getPath() ));
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }*/

       /* second step:
       * */
        URL url = Driver.class.getResource(ConstantList.mainPath) ;
        try{
            indexQuestionFiles( new File( url.getPath() ));
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        File tagFile = new File(url.getPath()+File.separator+"tags_filtered.txt") ;
        List<String> tagList = new ArrayList<String>() ;

        try{
            BufferedReader reader = new BufferedReader( new FileReader( tagFile )) ;
            String line ;
            while( (line = reader.readLine()) != null ){
                tagList.add(line.toLowerCase());
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch( IOException e){
            e.printStackTrace();
        }

        for( String eachTag : tagList ){
            try{
                System.out.println("SEARCHING : " + eachTag) ;
                searchAndWriteQuestionBasedOnGivenTag(url.getPath(), eachTag);
            } catch( IOException e ){
                e.printStackTrace();
            }
        }
    }

//----------------- function methods --------------------------
    /* first-part:
     * extract QuestionBean from Question_*.xml files
    *  @param inputDirectory is the path to each data flders
    *  */
    public static void indexQuestionFiles(File inputDirectory) throws FileNotFoundException{
        QuestionXmlParser parser = new QuestionXmlParser() ;
        QuestionIndexer indexer = new QuestionIndexer(inputDirectory.getAbsolutePath()+File.separator+QuestionIndexer.indexFolderName) ;

        if( inputDirectory.isDirectory()) {
            int i = 0;
            File dataDirectory = new File(inputDirectory.getAbsolutePath() + File.separator + QuestionXmlParser.questionFolderName);
            for( File file : dataDirectory.listFiles()){
                if ( !file.isDirectory()){
                    List<QuestionBean> questionBeans = parser.parseXml( new FileInputStream(file)) ;
                    System.out.println("PARSED : "+ file.getName()) ;
                    int testDataCount = (int)Math.floor(questionBeans.size()*0.1) ;
                    int trainDataCount = questionBeans.size() - testDataCount ;

                    System.out.println(trainDataCount) ;


                    try{
                        indexer.indexQuestions(questionBeans.subList(0,trainDataCount));
                        System.out.println("INDEXED: " + file.getName()) ;

                    } catch ( IOException e){
                        System.out.println("Error: could not write to index!!!") ;
                        e.printStackTrace();
                    }

                    writeTestDataToFile(questionBeans.subList(trainDataCount, questionBeans.size()), inputDirectory ,i++ );
                    System.out.println("Test Data : testQuestion_" +i + ".tsv") ;
                }
            }
        }

        indexer.close() ;
    }

    /*
    * first-part:
     * method to writer test QuestionBeans into file
    * under folder cleanedData/testData/
    * */
    private static void writeTestDataToFile(List<QuestionBean> testQuestionList, File inputDirectory , int fileNum ){
        PrintWriter writer  = null ;
        try{
            File testDataDir = new File(inputDirectory.getAbsolutePath() + "/testData/") ;
            if( testDataDir.exists())
                testDataDir.delete() ;
            testDataDir.mkdirs() ;
            writer = new PrintWriter(testDataDir.getAbsolutePath() + File.separator +"testQuestion_"+ fileNum +".tsv") ;

            for( QuestionBean question : testQuestionList){
                String questionText = question.getQuestionId() + "\t" +question.getQuestionContent()
                                        +"\t" +question.getQuestionTagForTestData() ;
                writer.println(questionText) ;
            }
            writer.close();

        } catch( FileNotFoundException e ){
            e.printStackTrace();
        }
    }

    /*
    * second-part:
    * */
    public static void searchAndWriteQuestionBasedOnGivenTag(String rootDir , String searchTag) throws IOException{
        QuestionSearcher searcher = QuestionSearcher.getMultiFieldQuestionSearcher(rootDir+File.separator+QuestionIndexer.indexFolderName, searchTag) ;
        ScoreDoc [] positiveResult = searcher.searchPositiveQuestionForGiveTag(searchTag) ;
        ScoreDoc [] negativeResult = searcher.searchNegativeQuestionForGivenTag(searchTag,positiveResult.length) ;

        System.out.println("Total POSITIVE Hits for " +searchTag + " : " +positiveResult.length ) ;
        System.out.println("Total NEGATIVE Hits for " +searchTag + " : " +negativeResult.length ) ;
        IndexSearcher indexSearcher = searcher.getIndexSearcher() ;
        FileWriter writer = null ;
        BufferedWriter bufferedWriter = null ;

        try{
            File trainDataDir = new File(rootDir +"/trainData/"+searchTag) ;
            trainDataDir.mkdirs() ; // make folder for all docs which contain the searchTag
            writer = new FileWriter(trainDataDir.getAbsolutePath()+File.separator+searchTag+"_positive.tsv") ;
            bufferedWriter = new BufferedWriter(writer) ;
            System.out.println("WRITTING positive : " + searchTag) ;
            for(ScoreDoc document : positiveResult){
                Document curDoc = indexSearcher.doc(document.doc) ;
                String documentContents = curDoc.get("qId") +"\t" + curDoc.get("qContent")+
                        "\t" + searchTag+"\n" ;
                bufferedWriter.write( documentContents);
            }
            bufferedWriter.close();
            writer.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        }

        try{
            File trainDataDir = new File(rootDir+"/trainData/"+searchTag) ;
            writer = new FileWriter(trainDataDir.getAbsolutePath()+File.separator+searchTag+"_negative.tsv") ;
            bufferedWriter = new BufferedWriter(writer) ;
            System.out.println("WRITING negative: "+ searchTag) ;
            for(ScoreDoc document : negativeResult ){
                Document curDoc = indexSearcher.doc(document.doc) ;
                String documentContents = curDoc.get("qId") +"\t" +curDoc.get("qContent") +"\tNOT_"+searchTag ;
                bufferedWriter.write(documentContents+"\n") ;
            }
            bufferedWriter.close();
            writer.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}
