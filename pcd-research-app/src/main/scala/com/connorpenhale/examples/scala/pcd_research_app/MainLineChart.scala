/*
 * Copyright 2013 ScalaFX Project
 * All right reserved.
 */
package com.connorpenhale.examples.scala.pcd_research_app

import java.io.File
import javax.imageio.ImageIO

import scalafx.application.JFXApp
import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.Scene
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.LineChart
import scalafx.scene.chart.NumberAxis
import scalafx.scene.chart.XYChart
import scalafx.scene.image.WritableImage

object BasicLineChart extends JFXApp {

  val xAxis = NumberAxis("Values for X-Axis", 0, 3, 1)
  val yAxis = NumberAxis("Values for Y-Axis", 0, 3, 1)

  // Helper function to convert a tuple to `XYChart.Data`
  val toChartData = (xy: (Long, Long)) => XYChart.Data[Number, Number](xy._1, xy._2)

  var series1 = new XYChart.Series[Number, Number] {
    name = "Series 1"
    data = Seq(
      (0L, 10L),
      (12L, 14L),
      (22L, 19L),
      (27L, 23L),
      (29L, 5L)).map(toChartData)
  }

  var series2 = new XYChart.Series[Number, Number] {
    name = "Series 2"
    data = Seq(
      (0L, 16L),
      (8L, 4L),
      (14L, 29L),
      (21L, 13L),
      (26L, 9L)).map(toChartData)
  }

  stage = new JFXApp.PrimaryStage {
    title = "Line Chart Example"
    scene = new Scene {
      root = {

        val lineChart = new LineChart[Number, Number](xAxis, yAxis, ObservableBuffer(series1, series2))
        lineChart.setAnimated(false)

        def savePng: Unit = {
          val img = lineChart.snapshot(null, new WritableImage(500, 250))
          val file = new File("/tmp/chart.png")
          ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file)
        }

        lineChart
      }
    }
  }
}

