package com.mairo.ukl.services

import java.util.Properties

object KafkaConnectionConfigurator {

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:29092")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("auto.offset.reset", "latest")
  props.put("group.id", "consumer-group")
}
