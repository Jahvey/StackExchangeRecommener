package org.kylin.zhang.StackExchange

import java.io.StringReader

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.util.Version
import org.apache.spark.mllib.classification.SVMModel
import org.kylin.zhang.StackExchange.indexer.service.StackExChangeAnalyzer

import scala.collection.mutable

/**
 * Created by Aimer on 2016/2/11.
 */
object Tokenizer {
  val LuceneVersion = Version.LUCENE_47

  def tokenizeAll(docs: Iterable[Document]) =docs.map(tokenize)

  def tokenize(doc:Document): TermDoc = TermDoc(doc.docId, doc.label, doc.integerLabel, tokenize(doc.body))

  def tokenize(content: String): Seq[String] ={
    val tReader  = new StringReader(content)
    val analyzer = new StackExChangeAnalyzer()
    val tStream  = analyzer.tokenStream("contents", tReader)
    val term     = tStream.addAttribute(classOf[CharTermAttribute])
    tStream.reset()

    val result = mutable.ArrayBuffer.empty[String]
    while(tStream.incrementToken()){
      val termValue = term.toString
      if( !(termValue matches ".*[\\d\\.].*")){
        result += term.toString
      }
    }
    result
  }

}

case class TermDoc(doc: String, label: Set[String], integerLable: Int,terms: Seq[String])
case class Document(docId: String, body: String ="", label: Set[String]=Set.empty, integerLabel: Int)
case class ClassifierModelAndDictionaries(svmModel : SVMModel = null, termDictionary : Dictionary = null, labelDictionary : Dictionary = null, idfs : Map[String, Double] = null, accuracy : Option[Double]) extends Serializable