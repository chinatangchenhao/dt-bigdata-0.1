package com.it18zhang.storm.group.shuffle.wc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

/**
 * 拓扑
 */
public class App {

    private static boolean isLocal = true;

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();

        //设置Spout
        builder.setSpout("wcspount", new WordCountSpout()).setNumTasks(2);

        //设置bolt
        builder.setBolt("split-bolt", new SplitBolt(), 2).shuffleGrouping("wcspount").setNumTasks(2);

        builder.setBolt("counter-bolt", new CountBolt(), 5).fieldsGrouping("split-bolt", new Fields("word")).setNumTasks(5);

        Config config = new Config();
        config.setDebug(true);

        if (isLocal) {
            //本地模式运行
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology("WC", config, builder.createTopology());
            Thread.sleep(20000);
            //停止集群,会触发最后一个bolt的clear方法，看到本例子的打印结果
            localCluster.shutdown();
        } else {
            //集群模式运行
            StormSubmitter.submitTopology("WC", config, builder.createTopology());
        }
    }
}
