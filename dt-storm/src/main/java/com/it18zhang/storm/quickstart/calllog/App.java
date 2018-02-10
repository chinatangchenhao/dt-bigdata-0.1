package com.it18zhang.storm.quickstart.calllog;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

/**
 * 拓扑
 */
public class App {

    private static boolean isLocal   = true;

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        //设置Spout
        builder.setSpout("spout", new CallLogSpout());
        //设置bolt
        builder.setBolt("creator-bolt", new CallLogCreatorBolt())
                .shuffleGrouping("spout");
        builder.setBolt("counter-bolt", new CallLogCounterBolt())
                .fieldsGrouping("creator-bolt", new Fields("call"));

        Config config = new Config();
        config.setDebug(true);

        if (isLocal) {
            //本地模式运行
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology("LogAnalyserStorm", config, builder.createTopology());
            Thread.sleep(10000);
            //停止集群,会触发最后一个bolt的clear方法，看到本例子的打印结果
            localCluster.shutdown();
        } else {
            //集群模式运行
            StormSubmitter.submitTopology("mytop", config, builder.createTopology());
        }
    }
}
