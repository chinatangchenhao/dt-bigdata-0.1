package com.it18zhang.storm.group.direct.wc;

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

    private int index = 0;

    private Random random;

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        //Util.sendToClient(this, "open()", 7777);
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
        /**
         * 获取taskId和组件名称的映射
         * 这里获得的结果可能是：
         * 1->_acker
         * 2->_acker
         * 3->split-bolt
         * 4->split-bolt
         * 5->wcspout
         * 6->wcspout
         */
        int taskId = 0;
        Map<Integer, String> map = context.getTaskToComponent();
        for (Map.Entry<Integer, String> e : map.entrySet()) {
            if (e.getValue().equals("split-bolt")) {
                taskId = e.getKey();
                break;
            }
        }
        if (index < 3) {
            String line = this.sentences.get(random.nextInt(4));
            //this.collector.emit(new Values(line));
            this.collector.emitDirect(taskId, new Values(line));
            Util.sendToLocalhost(this, line);
            index ++;
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
