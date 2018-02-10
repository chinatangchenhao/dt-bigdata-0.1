package com.it18zhang.storm.kafka.wc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.*;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.UUID;

/**
 * 拓扑
 */
public class App {

    private static boolean isLocal = true;

    /**
     * zk连接串
     */
    private static final String ZK_HOSTS = "s202:2181";

    /**
     * Kafka主题
     */
    private static final String TOPIC ="test4";

    /**
     * 信息在zk上的保存路径
     */
    private static final String ZK_ROOT ="/test4";

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();

        //配置zk连接串
        BrokerHosts hosts = new ZkHosts(ZK_HOSTS);

        //Spout配置
        SpoutConfig spoutConfig = new SpoutConfig(hosts, TOPIC, ZK_ROOT, UUID.randomUUID().toString());
        spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

        //设置Spout
        builder.setSpout("kafkaspout", kafkaSpout).setNumTasks(2);
        //设置bolt
        builder.setBolt("split-bolt" , new SplitBolt(), 2).shuffleGrouping("kafkaspout").setNumTasks(2);
        builder.setBolt("counter-bolt", new CountBolt()).fieldsGrouping("split-bolt", new Fields("word")).setNumTasks(4);

        Config config = new Config();
        config.setNumWorkers(2);
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
