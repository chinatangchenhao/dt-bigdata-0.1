package com.it18zhang.storm.ack.wc.online;

import com.it18zhang.storm.util.Util;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;
import java.util.Random;

public class SplitBolt implements IRichBolt {

    private TopologyContext context;

    private OutputCollector collector;

    private Random random;

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.context = context;
        this.collector = collector;
        this.random = new Random();
    }

    public void execute(Tuple tuple) {
        String line = tuple.getString(0);
        //Util.sendToLocalhost(this, ":" + line);
        String[] arr = line.split(" ");
        for (String s : arr) {
            this.collector.emit(new Values(s, 1));
        }
        /**
         * 此处模拟消息失败或者成功，线上代码没有实际意义
         */
        if (random.nextBoolean()) {
            //消息确认成功,然后spout的ack方法会对其进行回调成功处理
            this.collector.ack(tuple);
        } else {
            //消息确认失败,然后spout的fail方法会对其进行回调失败处理
            this.collector.fail(tuple);
        }

    }

    public void cleanup() {

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }

    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
