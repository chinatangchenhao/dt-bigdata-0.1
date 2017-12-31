package com.it18zhang.kafka.test;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestProducer {
    /**
     * 生产者
     */
    @Test
    public void testSend() {
        Properties props = new Properties();
        //设置broker列表
        props.put("metadata.broker.list","s202:9092");
        //设置串行化
        props.put("serializer.class","kafka.serializer.StringEncoder");
        //
        props.put("request.required.acks","1");

        //创建生产者配置对象
        ProducerConfig config = new ProducerConfig(props);

        //创建生产者
        Producer<String,String> producer = new Producer<String, String>(config);

        KeyedMessage<String, String> msg = new KeyedMessage<String, String>("test","100","hello world tom100");
        producer.send(msg);
        System.out.println("message is over!");
    }

    /**
     * 消费者
     */
    @Test
    public void testConsumer() {
        Properties props = new Properties();
        props.put("zookeeper.connect","s202:2181");
        props.put("group.id","g1");
        props.put("zookeeper.session.timeout.ms","500");
        props.put("zookeeper.sync.time.ms","250");
        props.put("auto.commit.interval","1000");
        //控制偏移量策略:smallest=fromBegining 表示从头开始消费 largest表示从最近的offset开始消费
        props.put("auto.offset.reset","smallest");
        //创建消费者配置对象
        ConsumerConfig config = new ConsumerConfig(props);

        Map<String,Integer> map = new HashMap<String, Integer>();
        map.put("test3",new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> msgs =
                Consumer.createJavaConsumerConnector(config).createMessageStreams(map);
        List<KafkaStream<byte[], byte[]>> msgList = msgs.get("test3");
        for (KafkaStream<byte[], byte[]> stream : msgList) {
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                byte[] message = it.next().message();
                System.out.println(new String(message));
            }
        }
    }

    /**
     * 生产者2
     */
    @Test
    public void testSend2() {
        Properties props = new Properties();
        //设置broker列表
        props.put("metadata.broker.list","s202:9092");
        props.put("bootstrap.servers","s202:9092");
        //设置串行化
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        //
        props.put("request.required.acks","0");

        ProducerRecord<String,String> record = new ProducerRecord<String, String>("test","101","jerry");
        org.apache.kafka.clients.producer.Producer<String,String> p = new KafkaProducer<String, String>(props);
        p.send(record);
        System.out.println("message is over!");
    }
}
