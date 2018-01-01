package com.it18zhang.kafka.partition;

import kafka.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * 自定义kafka简单区分器
 */
public class SimplePartitioner implements org.apache.kafka.clients.producer.Partitioner{
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        return 1;
    }

    public void close() {

    }

    public void configure(Map<String, ?> configs) {

    }

    /*public int partition(Object key, int numPartitions) {
        int partition = 0;
        int iKey = (Integer) key;
        if (iKey > 0) {
            partition = iKey % numPartitions;
        }
        return partition;
    }*/
}
