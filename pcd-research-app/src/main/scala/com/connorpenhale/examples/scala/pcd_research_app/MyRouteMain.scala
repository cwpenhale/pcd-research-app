package com.connorpenhale.examples.scala.pcd_research_app

import org.apache.camel.main.Main
import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import org.apache.camel.component.twitter.streaming.TwitterStreamingComponent

/**
 * A Main to run Camel with MyRouteBuilder
 */
object MyRouteMain extends RouteBuilderSupport {

	val FAIL_MESSAGE  = "Set TWITTER_ACCESS_TOKEN and TWITTER_ACCESS_TOKEN_SECRET in your system environment"
  val CONSUMER_FAIL_MESSAGE  = "Set TWITTER_CONSUMER_KEY and TWITTER_CONSUMER_SECRET in your system environment"

  def main(args: Array[String]) {
	  BasicLineChart.main(Array(""))
    val consumerKey = sys.env.getOrElse("TWITTER_CONSUMER_KEY", "FAULT")
    val consumerSecret = sys.env.getOrElse("TWITTER_CONSUMER_SECRET", "FAULT")
    val accessToken = sys.env.getOrElse("TWITTER_ACCESS_TOKEN", "FAULT")
    val accessTokenSecret = sys.env.getOrElse("TWITTER_ACCESS_TOKEN_SECRET", "FAULT")
    assert(accessToken.exists(t => t != "FAULT") && accessTokenSecret.exists(t => t != "FAULT"), FAIL_MESSAGE)
    assert(consumerKey.exists(t => t != "FAULT") && consumerSecret.exists(t => t != "FAULT"), CONSUMER_FAIL_MESSAGE)
    val main = new Main()
    // create the CamelContext
    val context = main.getOrCreateCamelContext()
    // add our route using the created CamelContext
    main.addRouteBuilder(new MyRouteBuilder(context))
    // must use run to start the main application
    main.run()
  }
}

  