import sys
from pyspark.sql.functions import udf
from pyspark.sql import SparkSession
from pyspark.sql.functions import explode
from pyspark.sql.functions import split
from pyspark.sql.functions import from_json, col
from pyspark.sql.types import StructField, StructType, StringType, DoubleType, TimestampType
from transformers import pipeline
import json

if __name__ == "__main__":

    # Host URL Kafka
    bootstrapServers = "localhost:9092"
    subscribeType = "subscribe"
    topics = "youtube-scraper-detail-video"

    # Start spark instance
    spark = SparkSession\
        .builder\
        .appName("KafkaYoutubeScraperSparkStream")\
        .master("local")\
        .getOrCreate()

    # Read topic from Kafka
    df = spark.readStream\
        .format("kafka")\
        .option("kafka.bootstrap.servers", bootstrapServers)\
        .option("subscribe", "json_topic")\
        .option(subscribeType, topics)\
        .option("failOnDataLoss", "false")\
        .load()
    df.printSchema()

    df.selectExpr("CAST(value AS STRING)") \
      .writeStream \
      .outputMode("append")\
      .format("kafka") \
      .option("kafka.bootstrap.servers", "localhost:9092") \
      .option("topic", "youtube-scraper-detail-video-analysed") \
      .option("checkpointLocation", "/tmp/youtube-scraper-detail-video-analysed")\
      .start() \
      .awaitTermination()




