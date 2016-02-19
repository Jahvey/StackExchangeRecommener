package org.kylin.zhang.StackExchange

import com.google.common.collect.ImmutableBiMap
import org.apache.spark.mllib.linalg.Vectors
import scala.collection.JavaConversions._

/**
 * Created by Aimer on 2016/2/11.
 */
class Dictionary (dict: Seq[String]) extends Serializable {

  // map term => index
  val termToIndex = ImmutableBiMap.builder[String,Int]()
                    .putAll(dict.zipWithIndex.toMap[String,Int])
                    .build()

  @transient
  lazy val indexToTerm = termToIndex.inverse()

  val count = termToIndex.size()

  def indexOf(term: String) = termToIndex(term)

  def valueOf(index: Int) = indexToTerm(index)

 def tfIdfs(terms: Seq[String] , idfs: Map[String,Double]) ={
    val filteredTerms = terms.filter(idfs contains)    // terms this Seq[String] will filter all the elements which appeared in Map[String,Double] 's key set
                                                        // and we get a new filtered Seq(List[String]) here : filteredTerms
        (filteredTerms.groupBy(identity).map{
          case (term, instances) =>                 // term is String , and instances is List of all terms with the same value ; instances.size() is the counter of the terms
            (indexOf(term), (instances.size.toDouble / filteredTerms.size.toDouble)*idfs(term))
        }).toSeq.sortBy(_._1) // idfs(term) is the value that weight value of the word term  ; 'term appear time' / 'total counter * term's weight
  } // finally we got (index of term , and the term's corresponding value )

  def vectorize(tfIdfs: Iterable[(Int, Double)]) ={
    Vectors.sparse(dict.size , tfIdfs.toSeq)
  }
}


object MatchPatternTester extends App{

  def getMapFromSeq(dict: Seq[String]): Unit ={
    val kTv = ImmutableBiMap.builder[String,Int]()
              .putAll(dict.zipWithIndex.toMap[String,Int])
              .build()


    val values = kTv.keySet()

    values.foreach( (x: String ) => println("key " + x +" value "+ kTv(x)))

   val inverseMap = kTv.inverse()

  val values2 = inverseMap.keySet()
    values2.foreach((k: Int ) => println("key " +k + " value "+ inverseMap(k)))

  }

  val listInput = List[String]("aimer","aimer","kylin","kokia","rurutia","kylin")

  getMapFromSeq(listInput)

  val result = listInput.groupBy(identity).map{
    case (term , instances) =>{
      println(term)
      println(instances)
    }
  }


  // how map .toSeq to List
  // just <k,v> to tuple (k,v) as a element in List
  val mapx = Map[Int,Double](1-> 5.1, 2->2.2)
  val listX = mapx.toSeq.sortBy(_._2)

  for ( x <- listX )
    println( x )
}
