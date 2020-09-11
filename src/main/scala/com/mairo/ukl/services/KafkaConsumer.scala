package com.mairo.ukl.services

import java.time.Duration
import java.util.Properties
import java.util.concurrent.{ExecutorService, Executors}

import scala.jdk.CollectionConverters._


object KafkaConsumer {

  import org.apache.kafka.clients.consumer.KafkaConsumer

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:29092")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("auto.offset.reset", "latest")
  props.put("group.id", "consumer-group")
  val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
  val executor: ExecutorService = Executors.newSingleThreadExecutor()

  def runConsumer(): KafkaConsumer[String, String] = {
    executor.execute(() => consumeFromKafka("quick-start"))
    consumer
  }

  def consumeFromKafka(topic: String): Unit = {
    consumer.subscribe(List(topic).asJava)
    while (true) {
      val record = consumer.poll(Duration.ofMillis(2000)).asScala
      for (data <- record.iterator)
        println(data.value())
    }
  }
}
