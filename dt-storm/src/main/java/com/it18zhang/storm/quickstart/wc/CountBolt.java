package com.it18zhang.storm.quickstart.wc;

import com.it18zhang.storm.util.Util;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

import java.util.HashMap;
import java.util.Map;

public class CountBolt implements IRichBolt {

    private TopologyContext context;

    private OutputCollector collector;

    private Map<String, Integer> map;

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        Util.sendToClient(this, "prepare()", 9999);
        this.context = context;
        this.collector = collector;
        this.map = new HashMap<String, Integer>();
    }

    public void execute(Tuple tuple) {
        Util.sendToClient(this, "execute("+tuple.toString()+")", 9999);
        String word = tuple.getString(0);
        Integer count = tuple.getInteger(1);
        if (!map.containsKey(word)) {
            map.put(word, 1);
        } else {
            map.put(word, map.get(word) + count);
        }
    }

    public void cleanup() {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
