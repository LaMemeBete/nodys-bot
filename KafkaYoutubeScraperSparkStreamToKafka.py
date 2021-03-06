import sys
from pyspark.sql.functions import udf
from pyspark.sql import SparkSession
from pyspark.sql.functions import explode
from pyspark.sql.functions import split
from pyspark.sql.functions import from_json, col
from pyspark.sql.types import StructField, StructType, StringType, DoubleType, TimestampType
from transformers import AutoTokenizer, AutoModelForTokenClassification
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
    spark.sparkContext.setLogLevel('WARN')

    # Read topic from Kafka
    df = spark.readStream\
        .format("kafka")\
        .option("kafka.bootstrap.servers", bootstrapServers)\
        .option("subscribe", "json_topic")\
        .option(subscribeType, topics)\
        .option("failOnDataLoss", "false")\
        .load()
    df.printSchema()

    tokenizer = AutoTokenizer.from_pretrained("dslim/bert-base-NER")
    model = AutoModelForTokenClassification.from_pretrained("dslim/bert-base-NER")
    nlp_ner = pipeline("ner", model=model, tokenizer=tokenizer, grouped_entities=True)

    def add_sentiment_score(data):
        text = json.loads(data)
        ner_results = nlp_ner(text['description'])
        arr_per = []
        arr_loc = []
        arr_misc = []
        arr_org = []
        arr_total = []
        for res in ner_results:
            arr_total.append(res['word'])
            if res['entity_group'] == 'PER':
                arr_per.append(res['word'])
            if res['entity_group'] == 'LOC':
                arr_loc.append(res['word'])
            if res['entity_group'] == 'ORG':
                arr_org.append(res['word'])
            if res['entity_group'] == 'MISC':
                arr_misc.append(res['word'])
        print("----------")
        print(ner_results)
        return json.dumps({'suggestion':text,
                           'organ': ' '.join(arr_org),
                           'per': ' '.join(arr_per),
                           'loc': ' '.join(arr_loc),
                           'misc': ' '.join(arr_misc),
                           'total': ' '.join(arr_total)})
    add_sentiment_score_udf = udf(add_sentiment_score, StringType())
    nerDF = df.selectExpr('CAST(value AS STRING)')
    nerDF = nerDF.withColumn("value", add_sentiment_score_udf(nerDF.value))
    nerDF.printSchema()

    nerDF.selectExpr("CAST(value AS STRING)") \
      .writeStream \
      .outputMode("append")\
      .format("kafka") \
      .option("kafka.bootstrap.servers", "localhost:9092") \
      .option("topic", "youtube-scraper-detail-video-analysed") \
      .option("checkpointLocation", "/tmp/youtube-scraper-detail-video-analysed")\
      .start() \
      .awaitTermination()




