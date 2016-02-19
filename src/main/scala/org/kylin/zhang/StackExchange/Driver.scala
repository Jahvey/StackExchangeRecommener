package org.kylin.zhang.StackExchange

import java.io.File

import org.apache.spark.{SparkConf, SparkContext}
import org.kylin.zhang.StackExchange.indexer.util.ConstantList

/**
 * Created by Aimer on 2016/2/12.
 */
object Driver extends App {

  var rootDir : String = null
  var clusterCount = 0
  var sc: SparkContext = null

  // 1. test train svm model
  val trainDataSetPath = new File (Driver.getClass.getResource(ConstantList.mainPath+"/"+ConstantList.trainDataSetPathName ).getPath).getAbsolutePath
  val conf = new SparkConf().setMaster("local").setAppName("Master URL of Spark")
  println( trainDataSetPath)

  sc = new SparkContext(conf)

/*

  def trainClassifiers(rootDir: String) ={
    val tagsFile = rootDir +"/" +"tags_filtered.txt"
    var tagsList: List[String] = List()
    for( line <- Source.fromFile(tagsFile).getLines()){
      tagsList ::= line.trim
    }

    var models: Map[String, ClassifierModelAndDictionaries] = Map()
    for( tag <- tagsList ){
      models += (tag -> createSVMModel(rootDir,tag,false))
    }

    models.foreach{
      modelData =>{
        val fileOutputStream = new FileOutputStream(rootDir +"/modelObjectData/" +modelData._1+".obj")
        val objectOutputStream = new ObjectOutputStream(fileOutputStream)
        objectOutputStream.close()
      }
    }
    sc.stop()
  }

  def createSVMModel(rootDir: String , tag: String , reportPerformance:Boolean) : ClassifierModelAndDictionaries ={


  }*/
}
