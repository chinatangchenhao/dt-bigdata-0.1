package com.it18zhang.storm.quickstart.wc;

import com.it18zhang.storm.util.Util;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordCountSpout implements IRichSpout {

    private TopologyContext context;

    private SpoutOutputCollector collector;

    private List<String> sentences;

    private Random random;

        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        Util.sendToClient(this, "open()", 7777);
        this.context = context;
        this.collector = collector;
        this.sentences = new ArrayList<String>();
        this.sentences.add("hello world tom");
        this.sentences.add("hello world tomas");
        this.sentences.add("hello world tomasLee");
        this.sentences.add("hello world tomson");
        this.random = new Random();
    }

    public void close() {

    }

    public void activate() {

    }

    public void deactivate() {

    }

    public void nextTuple() {
        Util.sendToClient(this, "nextTuple()", 7777);
        String line = this.sentences.get(random.nextInt(4));
        this.collector.emit(new Values(line));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void ack(Object msgId) {

    }

    public void fail(Object msgId) {

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("line"));
    }

    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
