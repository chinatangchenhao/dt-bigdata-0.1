package com.it18zhang.storm.group.skewdata.wc;

import com.it18zhang.storm.util.Util;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * CountBolt使用二次聚合，解决数据倾斜问题
 * 一次聚合到二次聚合采用Field分组，完成数据的最终统计
 * 一次聚合和上次split工作使用
 */
public class CountBolt implements IRichBolt {

    private TopologyContext context;

    private OutputCollector collector;

    private Map<String, Integer> map;

    /**
     * 最新清分时间
     */
    private long lastEmitTime = 0;

    /**
     * 清分时间片
     */
    private final long DURATION = 5000;

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        //Util.sendToClient(this, "prepare()", 9999);
        this.context = context;
        this.collector = collector;

        this.map = new HashMap<String, Integer>();
        this.map = Collections.synchronizedMap(map);

        /**
         * 定义一个线程会不断的判断触发条件，并且清分数据。
         */
        Thread thread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    emitData();
                }
            }
        };
        //设置为守护进程
        thread.setDaemon(true);
        thread.start();
    }

    private void emitData() {
        //q清分map
        synchronized (map) {

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                //向下一级bolt反射tuple
                collector.emit(new Values(entry.getKey(), entry.getValue()));
            }
            //清理内存数据
            map.clear();
        }

        //休眠5秒
        try {
            Thread.sleep(DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void execute(Tuple tuple) {
        //Util.sendToClient(this, "execute("+tuple.toString()+")", 9999);
        String word = tuple.getString(0);
        Util.sendToLocalhost(this, word);
        Integer count = tuple.getInteger(1);
        if (!map.containsKey(word)) {
            //map.put(word, 1);
            map.put(word, count);
        } else {
            map.put(word, map.get(word) + count);
        }

        /*
        //判断是否符合清理集合的条件,这里每5秒清空一次,符合条件向下一级bolt反射tuple
        long nowTime = System.currentTimeMillis();
        if (nowTime - lastEmitTime > DURATION) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                //向下一级bolt反射tuple
                collector.emit(new Values(entry.getKey(), entry.getValue()));
            }
            //清理内存数据
            map.clear();
            lastEmitTime = nowTime;
        }
        */
    }

    public void cleanup() {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }

    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
