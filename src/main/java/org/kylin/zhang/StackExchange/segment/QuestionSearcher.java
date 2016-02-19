package org.kylin.zhang.StackExchange.segment;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.kylin.zhang.StackExchange.indexer.service.QuestionIndexer;
import org.kylin.zhang.StackExchange.indexer.util.QuestionXmlParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aimer on 2016/2/6.
 */
public class QuestionSearcher {
    private static QuestionSearcher questionSearcher = null ;
    private static Logger logger = Logger.getLogger(QuestionSearcher.class) ;

    private IndexReader     indexReader ;
    private IndexSearcher   indexSearcher ;
    private MultiFieldQueryParser multiFieldQueryParser ;
    private QueryParser queryParser ;
    private Query query ;
    private Analyzer analyzer ;

    public QuestionSearcher (String indexDir ) throws IOException {
        indexReader = DirectoryReader.open(FSDirectory.open( new File( indexDir ))) ;
        indexSearcher = new IndexSearcher( indexReader ) ;
        analyzer = new WhitespaceAnalyzer( Version.LUCENE_47 ) ;
        String searchField = "qId" ;
        queryParser = new QueryParser( Version.LUCENE_47 , searchField , analyzer) ;
    }

    public QuestionSearcher(String indexDir , String searchTag ) throws IOException{
        indexReader = DirectoryReader.open( FSDirectory.open( new File(indexDir))) ;
        indexSearcher = new IndexSearcher(  indexReader ) ;
        analyzer = new WhitespaceAnalyzer(Version.LUCENE_47) ;
        String [] tagFields = {"qTag1","qTag2","qTag3","qTag4","qTag5"} ;
        multiFieldQueryParser = new MultiFieldQueryParser(Version.LUCENE_47, tagFields, analyzer) ;
    }

    public static QuestionSearcher getMultiFieldQuestionSearcher(String indexDir , String searchTag ) {
        try{
            questionSearcher = new QuestionSearcher(indexDir , searchTag) ;
            return questionSearcher ;
        } catch(IOException   e){
            logger.error("something goes wrong when trying to get multi-field question searcher ") ;
            e.printStackTrace();
        }
        return null ;
    }

    public static QuestionSearcher getSingleFieldSearcher (String indexDir ){
       try {
           if (questionSearcher == null)
               questionSearcher = new QuestionSearcher(indexDir);
            return questionSearcher ;
       } catch (Exception e){
           logger.error("something wrong happened during generate Question-Searcher object") ;
           e.printStackTrace();
       }
        return null ;
    }

    public ScoreDoc [] searchPositiveQuestionForGiveTag(String searchTag ) throws IOException {
        try{
            query = multiFieldQueryParser.parse(QueryParser.escape(searchTag)) ;
        } catch (ParseException e){
            logger.error("something goes wrong when searching positive question for given tag ") ;
            e.printStackTrace();
        }
        System.out.println("Here is the Query we got by passing searchTag : " + searchTag + " query " + query.toString()) ;
        ScoreDoc [] scoreDocs =  indexSearcher.search( query , 1000).scoreDocs;

        if ( scoreDocs != null && scoreDocs.length > 0)
            return scoreDocs ;
        else
            return null ;
    }

    public ScoreDoc [] searchNegativeQuestionForGivenTag(String searchTag , int count ) throws IOException {
        BooleanQuery negationQuery = null ;
        try{
            MatchAllDocsQuery everyDocsClause = new MatchAllDocsQuery() ;
            query = multiFieldQueryParser.parse(QueryParser.escape(searchTag)) ;
            negationQuery = new BooleanQuery() ;
            negationQuery.add( everyDocsClause, BooleanClause.Occur.MUST);
            negationQuery.add(query , BooleanClause.Occur.MUST_NOT);
            System.out.println(negationQuery.toString()) ;
        } catch( ParseException e){
            e.printStackTrace();
        }

        return indexSearcher.search(negationQuery, count).scoreDocs;
    }


    public ScoreDoc [] searchDocumentContentForDocIds( List<Integer> docIdList ) throws IOException{
        List<ScoreDoc> docList = new ArrayList<ScoreDoc>() ;
        for ( Integer docId : docIdList ){
            query = NumericRangeQuery.newIntRange("qId", docId , docId , true , true ) ;
            ScoreDoc [] result = (indexSearcher.search(query, 1).scoreDocs) ;
            if ( result != null && result.length > 0  )
             docList.add( result[0]);
        }

        if ( docList.size() > 0 )
            return (ScoreDoc[]) docList.toArray() ;
        else
            return null ;
    }

    public IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }



    public void close() throws IOException {
        this.indexReader.close();
    }

    public static void main (String [] args ) throws Exception {
        QuestionXmlParser questionXmlParser = new QuestionXmlParser();
        List<String> foldPathList = questionXmlParser.getQuestionDataSetFolderList();
        QuestionSearcher myQuestionSearcher = null ;

        for (String folderName : foldPathList) {
            // 1. get index's path name
            String indexResultPath = folderName + File.separator + QuestionIndexer.indexFolderName;

            logger.debug("index result folder " + indexResultPath);

            myQuestionSearcher = QuestionSearcher.getSingleFieldSearcher(indexResultPath);

            /* test doc id searcher */
            // 1. create a Integer List to store Integer
            List<Integer> docIdList = new ArrayList<Integer>() ;
            for ( int i = 0 ; i < 100 ; i++ )
                docIdList.add( i );

            // 2. call search result
            ScoreDoc [] scoreDocArray = myQuestionSearcher.searchDocumentContentForDocIds( docIdList ) ;

            // 3. output results
            if ( scoreDocArray != null && scoreDocArray.length > 0)
                for ( ScoreDoc scoreDoc : scoreDocArray){
                    System.out.println( scoreDoc.doc + " " + scoreDoc.score ) ;
                }


            /* test positive question tag searcher
             * tag -- 'linux'
             */
            String tag = "linux" ;
            myQuestionSearcher = QuestionSearcher.getMultiFieldQuestionSearcher(indexResultPath , tag) ;
            ScoreDoc [] tagScoreDocArray = myQuestionSearcher.searchPositiveQuestionForGiveTag(tag) ;


            if ( tagScoreDocArray != null && tagScoreDocArray.length > 0 )
                for ( ScoreDoc doc : tagScoreDocArray){
                    System.out.println( doc.doc +" " + doc.toString()) ;
                }
        }
    }
}
