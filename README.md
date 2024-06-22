# Kafka Client Metrics to Amazon CloudWatch with KIP-714

This project contains an example that shows how to push metrics from your Apache Kafka clients to [Amazon CloudWatch](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/WhatIsCloudWatch.html) using the [KIP-714: Client Metrics and Observability](https://cwiki.apache.org/confluence/display/KAFKA/KIP-714%3A+Client+metrics+and+observability). To use this feature, you must use a Kafka cluster with the version 3.7.0 or higher. It also requires the Kraft mode enabled, which is the new mode to run Kafka brokers without requiring Zookeeper.

## Getting started

Everything in this project is configured for you just experience the KIP-714 with little hassle. If you want to understand in details what is going on behind the scenes, this blog post provides you this. To get started, just execute the Docker Compose file. It will execute a Kafka broker and it will perform the required setup on it. It will also execute an OpenTelemetry collector. The OpenTelemetry collector will use your stored AWS credentials to connect with Amazon CloudWatch. If you have not configured your credentials using the AWS CLI, do it before proceeding.

```bash
docker compose up -d
```

💡 For the next steps, you must have a distribution of Apache Kafka installed in your machine.

Load a few records into the `load-test` topic. The command below loads `50K` records to trigger the producer metrics. Each record with a payload size of 1KB, sending 1K records every second.

```bash
kafka-producer-perf-test.sh --producer-props bootstrap.servers=localhost:9092 --throughput 1000 --num-records 50000 --record-size 1024 --topic load-test --print-metrics
```

You must also consume these records to trigger the consumer metrics. The command below loads the `50K` records from the `load-test` topic.

```bash
kafka-consumer-perf-test.sh --bootstrap-server localhost:9092 --messages 50000 --topic load-test --print-metrics
```

💡 Depending of the version of Apache Kafka you're using, you may see some warnings after executing this command. You can safely ignore them.

## Viewing the metrics at Amazon CloudWatch

Go to the AWS console and access Amazon CloudWatch. You should see a new namespace called `kafka-kip-714` with a lots of metrics for you to play with.

![Multiple metric groups](/images/cloudwatch.png)

If you want to visualize the record count you loaded into `load-test` topic using gauges, use the graph source below with Amazon CloudWatch:

```json
{
    "metrics": [
        [ "kafka-kip-714", "org.apache.kafka.producer.topic.record.send.total", "topic", "load-test" ],
        [ "kafka-kip-714", "org.apache.kafka.consumer.fetch.manager.records.consumed.total", "topic", "load-test" ]
    ],
    "view": "gauge",
    "stacked": false,
    "region": "us-east-1",
    "yAxis": {
        "left": {
            "min": 0,
            "max": 50000
        }
    },
    "stat": "Sum",
    "period": 300,
    "liveData": true,
    "setPeriodToTimeRange": false,
    "sparkline": true,
    "trend": true
}
```

You should see the following result:

![Sample Kafka metrics](/images/client-metrics.png)

# License

This project is licensed under the [Apache 2.0 License](./LICENSE).
