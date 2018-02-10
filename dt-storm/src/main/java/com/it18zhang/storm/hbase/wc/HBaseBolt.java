package com.it18zhang.storm.hbase.wc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HBaseBolt implements IRichBolt {

    private Table table;

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        try {
            Configuration conf = HBaseConfiguration.create();
            Connection connection = ConnectionFactory.createConnection(conf);
            TableName tname = TableName.valueOf("ns1:wordcount");
            this.table = connection.getTable(tname);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(Tuple tuple) {
        String word = tuple.getString(0);
        Integer count = tuple.getInteger(1);
        //使用hbasede的increment计数器进行wordcount
        try {
            this.table.incrementColumnValue(
                    Bytes.toBytes(word),  //rowkey
                    Bytes.toBytes("f1"), //column family
                    Bytes.toBytes("count"), //column
                    count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {}

    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
