package com.it18zhang.storm.ack.wc.online;

import com.it18zhang.storm.util.Util;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.*;

public class WordCountSpout implements IRichSpout {

    private TopologyContext context;

    private SpoutOutputCollector collector;

    private List<String> sentences;

    private int index = 0;

    private Random random;

    private static final int FAIL_RETRY = 3;

    /**
     * 消息集合，存放所有消息
     */
    private Map<Long, String> messages;

    /**
     * 失败消息
     */
    private Map<Long, Integer> failMessages;

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
        this.messages = new HashMap<Long, String>();
        this.failMessages = new HashMap<Long, Integer>();
    }

    public void close() {

    }

    public void activate() {

    }

    public void deactivate() {

    }

    public void nextTuple() {
        if (index < 3) {
            String line = this.sentences.get(random.nextInt(4));
            System.out.println(this+" nextTuple: " + line + ":" + index);
            //使用时间戳作为消息id，在生产环境会有严格生成器
            long ts = System.currentTimeMillis();
            messages.put(ts, line);
            //使用ts作为消息id
            this.collector.emit(new Values(line),ts);
            index ++;
        }
    }

    public void ack(Object msgId) {
        Long ts = (Long) msgId;
        failMessages.remove(ts);
        messages.remove(ts);
    }

    public void fail(Object msgId) {
        Long ts = (Long) msgId;
        //判断消息是否重试
        Integer count = failMessages.get(ts);
        count = (count == null) ? 0 : count;
        if (count >= FAIL_RETRY) {
            failMessages.remove(ts);
        } else {
            //重试发送
            collector.emit(new Values(messages.get(ts)), ts);
            count++;
            failMessages.put(ts, count);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("line"));
    }

    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
