package org.kylin.zhang.StackExchange.indexer.util;

/**
 * Created by Aimer on 2016/2/12.
 * Here is the file structure :
 *
 *           /data
 *              |- stop_words_set.txt
 *              |- <data-set-folder-name>/
 *                     |- cleanedData
 *                     |- indexResults
 *                     |- testData
 *                     |- trainData
 *                     |- tags_filtered.txt
 *
 */
public class ConstantList {
    public static String mainPath = "/data/unix.stackexchange.com" ;
    public static String stopWordsPath="/data/stop_words_set.txt" ;
    public static String tagsFileName = "tags_filtered.txt" ;
    public static String cleanedDataPathName ="cleanedData" ;
    public static String indexResultsPathName ="indexResults" ;
    public static String trainDataSetPathName ="trainData" ;
    public static String testDataSetPathName ="testData" ;
}
