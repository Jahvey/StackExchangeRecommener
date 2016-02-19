package org.kylin.zhang.StackExchange.indexer.service;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aimer on 2016/2/6.
 */
public class StackExChangeAnalyzer extends Analyzer{

    public static String stopWordFilePath = "/data/stop_words_set.txt" ;
    private final List<String> stopWordsList = new ArrayList<String>() ;
    private static Logger logger = Logger.getLogger(StackExChangeAnalyzer.class) ;

    public StackExChangeAnalyzer(){

        try{
            URL url = StackExChangeAnalyzer.class.getResource(stopWordFilePath) ;
            File f = new File(url.getPath()) ;

            if ( f.exists() && !f.isDirectory()){
                BufferedReader bufferedReader = new BufferedReader( new FileReader(f)) ;
                String line ;
                while( (line = bufferedReader.readLine()) != null){
                    stopWordsList.add(line);
                }
            }else{
                System.out.println("can not find file " + f.getAbsolutePath() ) ;
                logger.error("can not find file " + f.getAbsolutePath())  ;
            }

            if ( stopWordsList == null ){
                System.out.println("failed to extract stop words from file "+ f.getAbsolutePath()) ;
                logger.error("failed to extract stop words from file "+ f.getAbsolutePath());
            }
        } catch(Exception e){
            logger.error(e.getMessage()) ;
            e.printStackTrace();
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        LetterTokenizer tokenizer = new LetterTokenizer(Version.LUCENE_47, reader) ;

        TokenStream token = new LowerCaseFilter(Version.LUCENE_47, tokenizer) ;
        token = new StandardFilter(Version.LUCENE_47, token ) ;
        token = new PorterStemFilter(token) ;

        //initialize stop word filte analyzer by passing the stop-word list
        final CharArraySet stopSet = new CharArraySet(Version.LUCENE_47, stopWordsList.size() , true) ;
        stopSet.addAll(this.stopWordsList) ;
        stopSet.add(StopAnalyzer.ENGLISH_STOP_WORDS_SET) ;

        return new TokenStreamComponents(tokenizer, token ) ;
    }

    public static void main (String [] args ){

    }
}
