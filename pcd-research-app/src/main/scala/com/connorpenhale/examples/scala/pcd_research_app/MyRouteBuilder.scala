package com.connorpenhale.examples.scala.pcd_research_app

import java.io.File
import java.time.Instant
import java.time.LocalDateTime

import scala.collection.mutable.ListBuffer
import scala.io.Source

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.scala.dsl.builder.ScalaRouteBuilder

import javax.imageio.ImageIO
import scalafx.application.JFXApp
import scalafx.collections.ObservableBuffer
import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.Scene
import scalafx.scene.chart.LineChart
import scalafx.scene.chart.NumberAxis
import scalafx.scene.chart.XYChart
import scalafx.scene.image.WritableImage
import twitter4j.Status

/**
 * A Camel Router using the Scala DSL
 */
class MyRouteBuilder(override val context: CamelContext) extends ScalaRouteBuilder(context) {

  val consumerKey = sys.env.getOrElse("TWITTER_CONSUMER_KEY", "FAULT")
  val consumerSecret = sys.env.getOrElse("TWITTER_CONSUMER_SECRET", "FAULT")
  val accessToken = sys.env.getOrElse("TWITTER_ACCESS_TOKEN", "FAULT")
  val accessTokenSecret = sys.env.getOrElse("TWITTER_ACCESS_TOKEN_SECRET", "FAULT")
  val filter = "#blfc,#dragcon"
  val period = 10000
  val pcdDict = Source.fromFile("./PCDDict.txt").getLines().toSet

  class Counter {
    protected var c = 0L
    def ++ { c = c + 1L }
    def -- { c = c - 1L }
    def set(i: Long) = { c = i }
    def add(i: Long) = { c = c + i }
    def get = c
  }

  var blfcTSTotal = collection.mutable.Map[Long, Long]()
  var blfcTSPCD = collection.mutable.Map[Long, Long]()
  var dragconTSTotal = collection.mutable.Map[Long, Long]()
  var dragconTSPCD = collection.mutable.Map[Long, Long]()

  val tag = (exchange: Exchange) => {
    exchange.getIn.setHeader("type", if (exchange.getIn.getBody(classOf[Status]).getText.toLowerCase().contains("#blfc")) "blfc" else "dragcon")
    exchange.getIn.setHeader("user", exchange.getIn.getBody(classOf[Status]).getUser.getScreenName)
  }

  val detectSentiment = (exchange: Exchange) => {
    if (exchange.getIn.getBody != null) {
      val now = Instant.now().getEpochSecond
      val status = exchange.getIn.getBody(classOf[Status])
      val wordsInStatuses = new ListBuffer[String]
      wordsInStatuses.appendAll(status.getText.split(" "))
      val distinctWords = wordsInStatuses.distinct
      val truthiness = for (w <- distinctWords) yield pcdDict.contains(w)
      if (truthiness.contains(true)) {
        val user = status.getUser.getScreenName
        val theText = status.getText
        println(s"$user with #PCD says $theText")
        if (exchange.getIn.getHeader("blfc", classOf[String]).contains("#blfc")) {
          blfcTSPCD(now) = 1
        } else {
          dragconTSTotal(now) = 1
        }
      }
    }
  }
  
  val logIt = (exchange: Exchange) => {
    val now = LocalDateTime.now().toString()
    val totalTweets = blfcTSTotal.size + dragconTSTotal.size
    val bSize = blfcTSTotal.size
    val bpSize = blfcTSPCD.size
    val dSize = dragconTSTotal.size
    val dpSize = dragconTSPCD.size
    println(s"[$now] Total Tweets: $totalTweets. BLFC: $bpSize / $bSize. DragCon: $dpSize / $dSize")
    BasicLineChart.series1 = ObservableBuffer(new XYChart.Series[Number, Number] { 
      name = "BLFC"
      data = blfcTSTotal.
    }
  }

  val incrementOneTweet = (exchange: Exchange) => {
    if (exchange.getIn.getBody != null) {
      val now = Instant.now().getEpochSecond
      val body = exchange.getIn.getBody(classOf[Status])
      if (exchange.getIn.getHeader("type", classOf[String]).contains("blfc")) {
        blfcTSTotal(now) = 1
      } else {
        dragconTSTotal(now) = 1
      }
    }
  }

  s"twitter-streaming://filter?keywords=$filter&type=event&consumerKey=$consumerKey&consumerSecret=$consumerSecret&accessToken=$accessToken&accessTokenSecret=$accessTokenSecret" ==> {
    process(tag)
    log("${in.header.type}: ${in.header.user} says ${in.body.text}")
    process(incrementOneTweet)
    process(detectSentiment)
  }

  "timer://status?fixedRate=true&period=10000" ==> {
    process(logIt)
  }

}