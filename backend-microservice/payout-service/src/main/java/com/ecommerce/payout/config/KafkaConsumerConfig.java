package com.ecommerce.payout.config;
import com.ecommerce.payout.service.NonRetryableException;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

@Configuration
public class KafkaConsumerConfig {
 @Bean DefaultErrorHandler errorHandler(KafkaTemplate<Object,Object> template, MeterRegistry meters){
  DeadLetterPublishingRecoverer recoverer=new DeadLetterPublishingRecoverer(template,(r,e)->new TopicPartition(r.topic()+".DLT",r.partition()));
  recoverer.setFailIfSendResultIsError(true);
  ExponentialBackOffWithMaxRetries backoff=new ExponentialBackOffWithMaxRetries(5); backoff.setInitialInterval(2000); backoff.setMultiplier(2.0); backoff.setMaxInterval(30000);
  DefaultErrorHandler h=new DefaultErrorHandler((record,ex)->{String type=record.topic(); meters.counter("payout.event.dlt","event_type",type).increment(); recoverer.accept(record,ex);},backoff);
  h.addNotRetryableExceptions(NonRetryableException.class, IllegalArgumentException.class, com.fasterxml.jackson.core.JsonProcessingException.class);
  return h;
 }
 @Bean ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerContainerFactory(ConsumerFactory<String,String> consumerFactory, DefaultErrorHandler h){
  ConcurrentKafkaListenerContainerFactory<String,String> f=new ConcurrentKafkaListenerContainerFactory<>(); f.setConsumerFactory(consumerFactory); f.setCommonErrorHandler(h); return f;
 }
}
