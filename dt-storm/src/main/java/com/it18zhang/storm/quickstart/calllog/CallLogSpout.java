package com.it18zhang.storm.quickstart.calllog;

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

/**
 * 负责产生日志数据流
 */
public class CallLogSpout implements IRichSpout {

    /**
     * Spout输出收集器
     */
    private SpoutOutputCollector collector;

    /**
     * 是否完成
     */
    private boolean completed = false;

    /**
     * 上下文对象
     */
    private TopologyContext context;

    /**
     * 随机发生器
     */
    private Random randomGenerator = new Random();

    /**
     * 索引
     */
    private Integer idx = 0;

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.context = context;
        this.collector = collector;
    }

    public void close() {

    }

    public void activate() {

    }

    public void deactivate() {

    }

    /**
     * 如何产生下一个元祖
     */
    public void nextTuple() {
        if (this.idx <= 1000) {
            List<String> mobileNUmbers = new ArrayList<String>();
            mobileNUmbers.add("1234123401");
            mobileNUmbers.add("1234123402");
            mobileNUmbers.add("1234123403");
            mobileNUmbers.add("1234123404");

            Integer local = 0;
            while(local++ < 100 && this.idx++ < 1000) {
                //主叫
                String caller = mobileNUmbers.get(randomGenerator.nextInt(4));
                //被叫
                String callee = mobileNUmbers.get(randomGenerator.nextInt(4));
                while (caller.equals(callee)) {
                    callee = mobileNUmbers.get(randomGenerator.nextInt(4));
                }
                //模拟通话时长
                Integer duration = randomGenerator.nextInt(60);
                //向Blot发射元祖
                this.collector.emit(new Values(caller, callee, duration));
            }

        }
    }

    public void ack(Object msgId) {

    }

    public void fail(Object msgId) {

    }

    /**
     * 定义输出的字段名称
     *
     * @param declarer
     */
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("from", "to", "duration"));
    }

    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
