package visualization

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, lit, sum}
import org.apache.spark.sql.types.{DoubleType, LongType, StringType, StructField, StructType}
import plotly.Plotly._
import plotly._
import scalax.chart.PieChart
import scalax.chart.module.Exporting._

import java.io.File

object Visualization {

  object JsonFileFinder {
    def findJsonFiles(directoryPath: String): List[File] = {
      val directory = new File(directoryPath)
      val files = directory.listFiles

      files.filter(_.isFile).toList.filter(_.getName.endsWith(".json")) ++
        files.filter(_.isDirectory).toList.flatMap(f => findJsonFiles(f.getPath))
    }
  }

  def main(args: Array[String]): Unit = {

    var outputPath = "output"

    args
      .sliding(2,2)
      .toList
      .iterator
      .foreach{
        case Array("--outputPath", outputP: String) => outputPath = outputP
      }

    val spark = SparkSession
      .builder
      .appName("Chess")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // Data visualization Query 1

    val schemaQ1 = StructType(Array(
      StructField("eco", StringType, nullable = true),
      StructField("count", LongType, nullable = true)
    ))

    val q1 =
      spark
        .read
        .schema(schemaQ1)
        .json(JsonFileFinder.findJsonFiles(outputPath + "/q1").head.getPath)

    val xQ1 : Seq[String] = q1.select("eco").collect().toSeq.map(_.toString().replaceAll("[\\[\\].]", "")).filterNot(_.endsWith("-"))
    val yQ1: Seq[Int] = q1.select("count").collect().toSeq.map(_.toString().replaceAll("[\\[\\]]", "").toInt)

    Bar(xQ1, yQ1, name="Q1").plot()

    // Data visualization Query 3

    val schemaQ3 = StructType(Array(
      StructField("w_player", StringType, nullable = true),
      StructField("w_country", LongType, nullable = true),
      StructField("b_player", LongType, nullable = true),
      StructField("b_country", LongType, nullable = true)
    ))

    val q3 =
      spark
        .read
        .schema(schemaQ3)
        .json(JsonFileFinder.findJsonFiles(outputPath + "/q3").head.getPath)

    val statsUsQ3 = q3.withColumn("w_country_us", col("w_country").equalTo("US")).groupBy("w_country_us").count()

    val xWhiteUSQ3 : Seq[String] = Seq("US", "Other");
    val yWhiteUSQ3 : Seq[Double] = statsUsQ3.withColumn("percentage-white-US-people", col("count") /
      lit(statsUsQ3.select(sum("count")).collect()(0).getLong(0))).select("percentage-white-US-people").collect().toSeq.map(_.toString().replaceAll("[\\[\\]]", "").toDouble)

    Bar(xWhiteUSQ3, yWhiteUSQ3, name="White-US-Q3").plot()

    val statsEsQ3 = q3.withColumn("b_country_es", col("b_country").equalTo("ES")).groupBy("b_country_es").count()

    val xBlackESQ3 : Seq[String] = Seq("ES", "Other")
    val yBlackESQ3 = statsEsQ3.withColumn("percentage-black-ES-people", col("count") /
      lit(statsUsQ3.select(sum("count")).collect()(0).getLong(0))).select("percentage-black-ES-people").collect().toSeq.map(_.toString().replaceAll("[\\[\\]]", "").toDouble)

    Bar(xBlackESQ3, yBlackESQ3, name="Black-ES-Q3").plot()

    // Data visualization Query 4

    val schemaQ4 = StructType(Array(
      StructField("date", LongType, nullable = true),
      StructField("count", LongType, nullable = true)
    ))

    val q4 =
      spark
        .read
        .schema(schemaQ4)
        .json(JsonFileFinder.findJsonFiles(outputPath + "/q4").head.getPath)

    val xQ4 : Seq[Int] = q4.select("date").collect().toSeq.map(_.toString().replaceAll("[\\[\\]]", "").toInt)
    val yQ4 : Seq[Int] = q4.select("count").collect().toSeq.map(_.toString().replaceAll("[\\[\\]]", "").toInt)

    Bar(xQ4, yQ4, name="Q4").plot()

    // Data visualization Query 7

    val schemaQ7 = StructType(Array(
      StructField("w_result", StringType, nullable = true),
      StructField("b_result", StringType, nullable = true),
      StructField("count", LongType, nullable = true),
      StructField("percentage", DoubleType, nullable = true),
      StructField("result", StringType, nullable = true)
    ))

    val q7 =
      spark
        .read
        .schema(schemaQ7)
        .json(JsonFileFinder.findJsonFiles(outputPath + "/q7").head.getPath)

    val xQ7: Seq[String] =
      q7.select("result").collect().toSeq.map(_.toString().replaceAll("[\\[\\].]", ""))
    val yQ7 : Array[Double] =
      q7.select("percentage").collect().map(_.toString().replaceAll("[\\[\\].]", "").toDouble)

    val chart = PieChart(xQ7.zip(yQ7))

    // Save to JPEG

    ChartJPEGExporter(chart).saveAsJPEG("plot-4.jpeg")

  }

}
