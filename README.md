
# Nodys


## Installation


## Elasticsearch
### MacOS
Good reference to install elasticsearch on Mac: https://logz.io/blog/elk-mac/
#### Start service
```bash
brew services start elasticsearch
brew services start logstash
brew services start kibana
```
#### Stop services
```bash
brew services stop elasticsearch
brew services stop logstash
brew services stop kibana
```
#### List all services
```bash
brew services list
```
#### Access Kibana
```bash
http://localhost:5601
```
## Kafka
## Create topic in Kafka
This command should be executed only once
```bash
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic tweets
```
Depending on version could also be
```bash
kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic topic
```
## Run Kafka
Start Zookeeper (Kafka's orchestrator)
```bash
zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties
```
Start Kafka instance
```bash
kafka-server-start /usr/local/etc/kafka/server.properties
```
Run test consumer
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic tweets --from-beginning
```

List all topics
```bash
kafka-topics --list --bootstrap-server localhost:9092
```
## Spark
### Run Spark
```bash
spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.1.2 KafkaTweeterSparkStreamToKafka.py
```
OR
```bash
OBJC_DISABLE_INITIALIZE_FORK_SAFETY=YES spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.1.2 --jars elasticsearch-spark-30_2.12-7.13.2.jar KafkaTweeterSparkStream.py
```

### Spark location
```bash
cd /usr/local/Cellar/apache-spark
```
## Python
### Create virtual env
After running this command a folder with a python virtual env will be generated
```bash
python -m venv nodys
```
###Install requirements
```bash
pip install -r requirements.txt
```
### Activate python env
```bash
source nodys/bin/activate
```

### Check packages
```bash
pip list
```

### Check Python version
```bash
which python
```