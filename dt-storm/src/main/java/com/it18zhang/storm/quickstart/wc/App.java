package com.it18zhang.storm.quickstart.wc;

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
        builder.setSpout("wcspount"
                , new WordCountSpout()
                //设置Spout的并发暗示为3，每个任务都会运行在一个Executor执行线程里面
                //,3
               )
                /**
                 * 设置多个Spout，
                 * 比如setNumTasks(2)代表设置了2个Spout，
                 * 运行时在拓扑中会有2个WordCountSpout实例在Storm集群中并行的运行
                 */
                .setNumTasks(2);

        //设置bolt
        builder.setBolt("split-bolt"
                , new SplitBolt()
                //设置Bolt的并发暗示为4
                //, 4
                )
                .shuffleGrouping("wcspount")
                /**
                 * 设置多个Bolt，
                 * 比如setNumTasks(3)代表设置了2个Bolt，
                 * 运行时在拓扑中会有3个SplitBolt实例在Storm集群中并行的运行，
                 * 接受上游2个WordCountSpout实例所产生的数据
                 */
                .setNumTasks(3);

        builder.setBolt("counter-bolt", new CountBolt())
                .fieldsGrouping("split-bolt", new Fields("word"))
                .setNumTasks(4);

        Config config = new Config();
        //设置Worker工作进程数
        //config.setNumWorkers(3);
        config.setDebug(true);


        if (isLocal) {
            //本地模式运行
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology("WC", config, builder.createTopology());
            Thread.sleep(10000);
            //停止集群,会触发最后一个bolt的clear方法，看到本例子的打印结果
            localCluster.shutdown();
        } else {
            //集群模式运行
            StormSubmitter.submitTopology("WC", config, builder.createTopology());
        }
    }
}
