package com.mairo.ukl.services

import java.util.Properties

import cats.effect.Sync
import cats.syntax.either._
import com.mairo.ukl.utils.Flow
import com.mairo.ukl.utils.Flow.Flow

object KafkaProducer {

  import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:29092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String, String](props)

  def writeToKafka[F[_] : Sync](value: String): Flow[F, Unit] = {
    Flow.fromFResult(Sync[F].delay(writeToKafka("quick-start", value).asRight[Throwable]))
  }


  private def writeToKafka(topic: String, value: String): Unit = {
    val record = new ProducerRecord[String, String](topic, "key", s"$value")
    producer.send(record)
  }
}
