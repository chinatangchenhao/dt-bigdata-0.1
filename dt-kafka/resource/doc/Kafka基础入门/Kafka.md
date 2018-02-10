1.JMS简介

  1.1 什么是JMS?
      JMS(Java Message Service)Java消息服务规范，是用于异构(不同语言，不同平台下的不同应用或者程序)系统间的通信。
  1.2 MiddleWare(中间件)
      简单结构如下:
	    系统A ----> 消息服务(中间件) <---- 系统B
	  系统A为生产者，系统B为消费者，这是典型的生产消费关系。
	  对于消息系统来说有两个存储机制:
	      (1)P2P模式:生产者将消费发送到消息系统的队列(Queue)中,然后只能有一个消费者去消费这个消息。
		  (2)发布和订阅模式(Publish-Subscribe)+主题模式:可以把队列理解成一个主题(topic)。
		  生产者将消息发布到主题上，多个不同的消费者去订阅这个主题。例如Kafka(分布式消息中间件)。
      优点:方便异构系统之前的解耦合。
	  注意:对于Kafka来说，消费者将消息发送到主题上，消费者去订阅这个主题。中Kafka中有消费者组这个概念，对消费者进行了分组，同一个消费者组中只能有一个消费者消费主题中的消息。
	  

2.Kafka介绍,安装和基本操作

  2.1 Kafka介绍
      (1)分布式流处理平台
      (2)在系统之间构建实时数据流管道
      (3)以Topic分类对记录进行存储，每个记录包含K-V和时间戳	
	  (4)每秒百万消息吞吐量
  2.2 Kafka相关构成
      (1)Producer(消息生产者)
      (2)Consumer(消息消费者)
      (3)Consumer Group(消费者组)
      (4)Kafka Server(Broker)
      (5)Topic(主题)有副本+分区
      (6)Zookeeper
  2.3 安装
      (0)选择s202~s204三台主机安装kafka
      (1)JDK安装
      (2)准备ZK环境
      (3)解压安装包
	     tar -zxvf /mnt/hgfs/downloads/bigdata/kafka_2.11-0.10.0.1.tgz -C .
		 创建软链接
		 ln -s kafka_2.11-0.10.0.1 kafka
      (4)配置环境变量
	     vim /etc/profile
		 加入下面的配置信息:
		   export KAFKA_HOME=/soft/kafka
		   export PATH=$PATH:$KAFKA_HOME/bin
		 source /etc/profile
		 将kafka解压文件和环境变量配置文件分发到s203,s204重复步骤(4)
      (5)配置kafka集群
	     备份原始配置文件:
		 cd kafka/config/
		 cp server.properties server.properties.bak
		 配置相关参数
	     vim kafka/config/server.properties
		 配置如下内容
		 #配置kafka server的唯一标识
		 broker.id=202
		 #配置监听
		 listeners=PLAINTEXT://:9092
		 #配置数据存放目录
		 log.dirs=/home/centos/kafka/logs
		 #分区数配置(默认为1)
		 num.partitions=1
		 #zk连接
		 zookeeper.connect=s201:2181,s202:2181,s203:2081
		 #zk连接超时时间
		 zookeeper.connection.timeout.ms=6000
		 
		 将server.properties分发到s203和s204，并修改broker.id为对应主机的IP
      (6)启动kafka
	     <a>启动zk集群
		 <b>在s202~s204分别启动kafka
		    $>bin/kafka-server-start.sh -daemon config/server.properties
		 <c>验证kafka服务器是否启动成功
		    $>netstat -anop | grep 9092
      (7)创建主题
	     登入s202
         $>bin/kafka-topics.sh --create \
		                       --zookeeper s201:2181 \
							   --replication-factor 3 \
							   --partitions 3 \
							   --topic test
		 【注意】这里的replication-factor指定主题副本数为3，它必须小于broker节点个数。
		 下面这样创建主题是不正确的:
		 $>bin/kafka-topics.sh --create \
					           --zookeeper s202:2181 \
					           --replication-factor 4 \
					           --partitions 4 \
					           --topic test
		 【结果显示】
		  ...
		  ERROR while executing topic command:replication factor: 4 larger than available brokers: 3
		  ERROR kafka.admin.AdminOperationException: replication factor: 4 larger than available brokers: 3
		  ...
      (8)查看主题列表
         $>bin/kafka-topics.sh --list --zookeeper s201:2181
         【结果显示】
          test
      (9)通过cli启动控制台生产者
	     登入s202
	     $>bin/kafka-console-producer.sh --broker-list s202:9092 --topic test
	  (10)通过cli启动控制台消费者
	     登入s203
	     $>bin/kafka-console-consumer.sh --bootstrap-server s202:9092 --topic test --zookeeper s202:2181 --from-beginning
		 【注意】这里的--from-beginning代表从头开始消费,这意味着kafka主题中的消息是可以重复消费的
	  (11)在生产者控制台输入hellowrold
         登入s202输入hellowrold,然后登入s203查看控制台上收到hellowrold消息。
  2.4其他操作
	 删除主题
	 $>bin/kafka-topics.sh --zookeeper s201:2181 --topic test --delete
	 【结果显示】
	  Topic test is marked for deletion.
	  Note: This will have no impact if delete.topic.enable is not set to true.		
     【注意】该主题只是被标识为删除，并没有永久删除，并且该信息会被注册到zk的/admin/delete_topics路径下面。当时再次创建该主题是会报如下错误:
	  ERROR kafka.common.TopicExistsException:Topic "test" already exists.
	  ...
	  所以在删除主题的之外还需要删除zk上的路径即可彻底删除：
	  rmr /brokers/topics/test
	  rmr /admin/delete_topics/test

	  
3.Kafka集群在Zookeeper上的配置

	$cd zk
	$zkCli.sh -server s202:2181
	【结果显示】
	...
	[zk: s202:2181(CONNECTED) 0] ls /
	[controller,controller_epoch,brokers,zookeeper,yarn-leader-election,hadoop-ha,admin,isr_change_notification,consumers,config,hbase]
	...
	
	提取zk上的一些有关kafka的配置信息
	①/controller
	【节点数据】 {"version":1,"brokerid":202,"timestamp":"1490926369148"}
	 
	②/controller_epoch
	【节点数据】 1
	 
	③/brokers
	【叶子节点】[ids,topics,seqid]
	
	   /brokers/ids (该节点下包含了当前kafka集群下每个节点的信息)
       【叶子节点】 [202,203,204]
	     
		  /brokers/ids/202 (该节点下注册了kafka集群下某个节点连接信息)
		  /brokers/ids/203
		  /brokers/ids/204
		  【节点数据】 {"jmx_port":-1,"timestamp":"1490926370304","endpoints":["PLAINTEXT://s202:9092"],"host":"s202","version":3,"port":9092}
	   
	   /brokers/topics (该节点下包含了当前kafka集群中的每个topic和topic对应分区的信息)
	   /brokers/topics/test
	   /brokers/topics/test/partitions
	   /brokers/topics/test/partitions/0/state (该节点下记录了某个topic下某个分区的信息)
	   【节点数据】 {"controller_epoch":1,"leader":203,"version":1,"leader_epoch":0,"isr":[203,204,202]}
	   【注意】可以看出topic下的某个partition有leader和follower，s203为test这个主题在0号分区下的leader,
	   /brokers/topics/test/partitions/1/state
	   【节点数据】 {"controller_epoch":1,"leader":204,"version":1,"leader_epoch":0,"isr":[204,202,203]}
	   /brokers/topics/test/partitions/2/state
	   【节点数据】 {"controller_epoch":1,"leader":202,"version":1,"leader_epoch":0,"isr":[202,203,204]}
	
	   /brokers/seqid 
	   【节点数据】 null
	   
	④/admin
	
	  /admin/delete_topics (该节点记录删除主题的信息)
	  
	⑤/isr_change_notification
	  【节点数据】 null
	  
	⑥/consumers (该节点记录消费者组以及每个组下的消费者的相关信息)
	【注意】该节点为临时节点，比如当控制台消费者退出时，这个消费者在zk上的信息会被注销。
	【叶子节点】
	/consumers/console-consumer-61901(记录了有哪些消费者组,这里console-consumer-61901为消费者组)
	/consumers/console-consumer-61901/ids(记录了当前消费者组下有哪些消费者)
	/consumers/console-consumer-61901/ids/console-consumer-61901_s202-1490929423449-f7f4473e (记录了消费者的信息，比如console-consumer-61901_s202-1490929423449-f7f4473e这个消费者属于console-consumer-61901这个消费者组)
	
	/consumers/console-consumer-61901/owners
	/consumers/console-consumer-61901/owners/test (记录了kafka的主题被哪些消费者组消费了)
	/consumers/console-consumer-61901/owners/test/0
	/consumers/console-consumer-61901/owners/test/1
	/consumers/console-consumer-61901/owners/test/2
	
	/consumers/g2/offsets/test3/0 (记录了每个消费者组消费哪些主题的哪些分区下的offset标记)
	【节点数据】 2
	/consumers/g2/offsets/test3/1
	【节点数据】 1
	/consumers/g2/offsets/test3/2
	【节点数据】 1
	/consumers/g2/offsets/test3/3
	【节点数据】 1
	/consumers/g2/offsets/test3/4
	【节点数据】 1
	
	⑦/config
    	
4.Kafka数据文件日志

    创建一个5个分区，副本因子2的主题：	
	$>bin/kafka-topics.sh --create \
						   --zookeeper s202:2181 \
						   --replication-factor 2 \
						   --partitions 5 \
						   --topic test2
	这样在kafka的log下会产生2*5=10个数据日志文件夹。
	
	在s202下查看数据文件：
	ll /home/centos/kafka/logs
	【结果显示】
	...
	test2-1
	test2-2
	test2-3
	...
	
	在s203下查看数据文件：
	ll /home/centos/kafka/logs
	【结果显示】
	...
	test2-0
	test2-2
	test2-3
	test2-4
	...
	
	在s204下查看数据文件：
	ll /home/centos/kafka/logs
	【结果显示】
	...
	test2-0
	test2-1
	test2-4
	...
	
	从上面的结果可以看出kafka是以${TopicName}-{PartitionNo}来组织数据文件的,这些文件会尽量均匀的分散在kafka集群中。
	
5.Kafka手动再平衡

	重新布局分区和副本
	$>kafka-topic.sh --create
	                 --zookeeper s202:2181
					 --topic test2
					 --replica-assignment 203:204,203:204,203:204,203:204,203:204
	replica-assignment 指定的字符串每个partition用逗号分开，每个partition在平衡到哪些broker上用冒号分开。
	
	查看kafka运行日志观察分区是否改变:
	tail /soft/kafka/logs/controller.log
	【结果显示】
	...
	[...] INFO [AddPartitionListener on 202]: Partition modification triggered {"version":1,"partitions":{"
	4":[204,203],"1":[204,202],"0":[203,204],"2":[202,203],"3":[203,202]}} for path /brokers/topics/test2 {
	kafka.controller.PartitionStateMachine$PartitionModificationListener}
	...
	
	通过zk观察topic信息：
	get /brokers/topics/test2/partitions/0/state
	【结果显示】
	{"controller_epoch":3,"leader":203,"version":1,"leader_epoch":2,"isr":[203,204]}
	...
	
	在s202上观察kafka数据文件加
	ll kafka/logs
	【结果显示】
	...
	test3-0
	test3-1
	test3-2
	test3-3
	test3-4
	...
	
	ll kafka/logs/test3-0
	【结果显示】
	00000000000000000000.index
	00000000000000000000.log
	
	从上面的结果可以看出test2的0号分区的leader是s203,follower为s204，如果杀死s203我们在观察zk上的分区信息:
	【结果显示】
	{"controller_epoch":3,"leader":204,"version":1,"leader_epoch":2,"isr":[204]}
	...
	
6.Kafka的副本

	(1)broker存放消息是以消息到到达的顺序存放的。
	(2)生产者和消费都是副本感知的。
	(3)主题下每个分区都有leader和follower。
	
	6.1 partition leader
	(1)leader挂掉时，消息分区写入到本地log或者向生产者发送消息确认回执之前，生产者向新的leader发送消息。
	(2)新leader的选举是通过isr进行的，第一个注册的follower成为leader。
	
	6.2 Kafka支持的副本模式
	(1)Synchronous replication(同步副本)，复制流程如下:
	   ①producer连接zk识别topic下的某个partition的leader
	   ②producer向leader发送消息
	   ③leader收到消息写入到本地log数据文件中
	   ④follower从leader拉取消息
	   ⑤follower向本地写入log数据文件
	   ⑥follower向leader发送ack确认该消息同步完成
	   ⑦leader收到所有follower的ack消息
	   ⑧leader向producer回传ack确认该消息处理完毕
	(2)Asynchronous replication(异步副本)
	   和同步副本复制流程的区别在于:在于leader写入本地log数据文件之后直接向producer回传ack消息，不需要等待所有的follower复制完成。
	   
	6.3 Kafka生产者发送消息分析
	    Kafka生产者是线程安全的，它维护了本地的buffer pool，即发送消息时，消息进入pool。
	    Kafka发送消息时异步的，发送后立即返回
	    ack=all导致记录完全提交时阻塞
	    
	    源码流程大致如下:
	    (1)KafkaProducer.send(rec)
	    (2)KafkaProducer.doSend(rec,callback)
	    (3)interceptors.onSend()拦截器方法对消息进行批处理
	    (4)对K-V进行串行化
	    (5)计算分区
	    (6)计算消息的total size
	    (7)创建TopicPartition对象
	    (8)拦截器调用callback回调进行后置处理
	    (9)Sender进行发送[重要]
	    
7.Kafka Java API基础

   7.1 Kafka Producer API
       Old API代码:com.it18zhang.kafka.test.TestProducer#testSend
	   New API代码:com.it18zhang.kafka.test.TestProducer#testSend2
   7.2 Kafka Consumer API
       代码:com.it18zhang.kafka.test.TestProducer#testConsumer
   7.3 Kafka自定义分区器
       分区器:com.it18zhang.kafka.partition.SimplePartitioner
       代码:com.it18zhang.kafka.test.TestProducer#testSendByCustomPartitioner
   
  