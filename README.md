# Kafka Client Metrics to Amazon CloudWatch with KIP-714

This project contains an example that shows how to push metrics from your Apache Kafka clients to [Amazon CloudWatch](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/WhatIsCloudWatch.html) using the [KIP-714: Client Metrics and Observability](https://cwiki.apache.org/confluence/display/KAFKA/KIP-714%3A+Client+metrics+and+observability). To use this feature, you must use a Kafka cluster with the version 3.7.0 or higher. It also requires the Kraft mode enabled, which is the new mode to run Kafka brokers without requiring Zookeeper.

## Getting started

Everything in this project is already packed for you just experience the KIP-714 without much hassle. If you want to understand in details what is going on behind the scenes, this blog post provides you this. To get started with the project, just execute the Docker Compose file to execute a Kafka broker and a OpenTelemetry collector.

```bash
docker compose up -d
```

The current implementation will use your stored AWS credentials to connect with Amazon CloudWatch. If you have not configured your credentials using the AWS CLI, go ahead and do it. You may need to restart the containers for the change to take effect.

💡 For the next steps, you must have a distribution of Apache Kafka installed in your machine.

The next step is enable client metrics collection in your Kafka broker. Without this, the support for the KIP-714 won't be offcially enabled. Execute the following command for this:

```bash
kafka-client-metrics.sh --bootstrap-server localhost:9092 --alter --generate-name \
  --metrics org.apache.kafka.producer.,org.apache.kafka.consumer. \
  --interval 1000
```

Now create a topic to play with:

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic load-test --partitions 1 --replication-factor 1
```

Load a few records into the topic. Okay, I said a few but let's make it worth. With the command below you can load 50K records and trigger the publishing of the producer metrics.

```bash
kafka-producer-perf-test.sh --producer-props bootstrap.servers=localhost:9092 --throughput 1000 --num-records 50000 --record-size 1024 --topic load-test --print-metrics
```

Finally, you must consume these records to trigger the consumer metrics. Use the command below for this:

```bash
kafka-consumer-perf-test.sh --bootstrap-server localhost:9092 --messages 50000 --topic load-test --print-metrics
```

💡 Depending of the version of Apache Kafka you're using, you may see some warnings after executing this command. You can safely ignore them.

## Viewing the metrics at Amazon CloudWatch

Go to the AWS console and access Amazon CloudWatch. You should see a new namespace called `kafka-kip-714` with a bunch of metrics for you to play with.

![Apache Kafka Client Metrics](/images/cloudwatch.png)

# License

This project is licensed under the [Apache 2.0 License](./LICENSE).
