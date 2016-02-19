package org.kylin.zhang.StackExchange

import scala.collection.mutable
import scala.io.Source


/**
 * Created by Aimer on 2016/2/12.
 */
object DataParser {
  var currentLabel : String = null
  var currentIntegerLabel: Int = -1

  def parseAll(dataFiles : Iterable[String]) = dataFiles flatMap parse

  def parse(dataFile : String )={
    val docs = mutable.ArrayBuffer.empty[Document]

    var currentDoc: Document = null
    if(dataFile.contains("positive")){
      currentLabel = "positive"
      currentIntegerLabel = 1
    }
    if(dataFile.contains("negative")){
      currentLabel = "negative"
      currentIntegerLabel = 0
    }

    // here we begin read each line from current file

    /**
     * here is the structure of the file we read in
     * 'aix_negative.tsv'
     * 2	Distros that support compiling from source null	NOT_aix
     *
     * 'aix_positive.tsv'
     * */
    for( line <- Source.fromFile(dataFile).getLines()){
      var lineContents = line.split("\\t")
      var temp: Set[String] = Set.empty
      temp += currentLabel
      currentDoc = Document(lineContents(0),lineContents(1).substring(0,lineContents.indexOf("null")) +" " + lineContents(2) , temp , currentIntegerLabel)
      docs += currentDoc
    }
    docs
  }
}
