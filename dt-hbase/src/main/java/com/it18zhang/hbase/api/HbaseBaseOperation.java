package com.it18zhang.hbase.api;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Hbase基本操作
 */
public class HbaseBaseOperation {
    /**
     * put操作
     *
     * @throws Exception
     */
    @Test
    public void put() throws Exception {
        //创建conf对象
        Configuration conf = HBaseConfiguration.create();
        //通过Hbase连接工厂创建连接
        Connection conn = ConnectionFactory.createConnection(conf);
        //通过连接查询table对象
        TableName tname = TableName.valueOf("ns1:t1");
        Table table = conn.getTable(tname);

        //通过Bytes工具类将字符串转成字节数组
        byte[] rowkey = Bytes.toBytes("row3");
        //创建Put对象
        Put put = new Put(rowkey);

        byte[] f1 = Bytes.toBytes("f1");
        byte[] id = Bytes.toBytes("id");
        byte[] value = Bytes.toBytes(102);
        put.addColumn(f1,id,value);

        //插入数据
        table.put(put);
    }

    /**
     * get操作
     *
     * @throws Exception
     */
    @Test
    public void get() throws Exception {
        //创建conf对象
        Configuration conf = HBaseConfiguration.create();
        //通过Hbase连接工厂创建连接
        Connection conn = ConnectionFactory.createConnection(conf);
        //通过连接查询table对象
        TableName tname = TableName.valueOf("ns1:t1");
        Table table = conn.getTable(tname);

        //通过Bytes工具类将字符串转成字节数组
        byte[] rowkey = Bytes.toBytes("row3");
        Get get = new Get(rowkey);
        Result r = table.get(get);
        byte[] idValue = r.getValue(Bytes.toBytes("f1"),Bytes.toBytes("id"));
        System.out.println(Bytes.toString(idValue));
    }

    /**
     * 大批量put
     * 注意:为了提高程序性能有两个可以设置:(1)关闭WAL(2)关闭自动清理缓冲区
     *
     * @throws Exception
     */
    @Test
    public void batchPut() throws Exception {
        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0000");//0代表占位

        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
        HTable table = (HTable) conn.getTable(tname);
        //关闭自动清理缓冲区，默认是写一条记录提交一个
        table.setAutoFlush(false);

        byte[] f1 = Bytes.toBytes("f1");

        byte[] id = Bytes.toBytes("id");
        byte[] name = Bytes.toBytes("name");
        byte[] age = Bytes.toBytes("age");
        long start= System.currentTimeMillis();
        for (int i = 4; i < 10000; i++) {
            Put put = new Put(Bytes.toBytes("row" + format.format(i)));
            //关闭写前日志
            put.setWriteToWAL(false);
            put.addColumn(f1,id,Bytes.toBytes(i));
            put.addColumn(f1,name,Bytes.toBytes("tom" + i));
            put.addColumn(f1,id,Bytes.toBytes(i % 100));
            table.put(put);
            //每2000条作为一个批次提交
            if (i% 200 == 0) {
                table.flushCommits();
            }
        }
        //
        table.flushCommits();
        System.out.println("Time Cost:" + (System.currentTimeMillis() - start));
    }

    /**
     * 创建名字空间
     *
     * @throws Exception
     */
    @Test
    public void createNamespace() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        //通过Hbase连接工厂创建连接
        Connection conn = ConnectionFactory.createConnection(conf);
        Admin admin = conn.getAdmin();
        //创建名字空间描述符
        NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create("ns2").build();
        admin.createNamespace(namespaceDescriptor);
    }

    /**
     * 获取名字空间
     *
     * @throws Exception
     */
    @Test
    public void listNamespaces() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        //通过Hbase连接工厂创建连接
        Connection conn = ConnectionFactory.createConnection(conf);
        Admin admin = conn.getAdmin();

        //查询名字空间
        NamespaceDescriptor[] namespaceDescriptors = admin.listNamespaceDescriptors();
        for (NamespaceDescriptor  ns : namespaceDescriptors) {
            System.out.println(ns.getName());
        }
    }

    /**
     * 创建表
     *
     * @throws Exception
     */
    @Test
    public void createTable() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        //通过Hbase连接工厂创建连接
        Connection conn = ConnectionFactory.createConnection(conf);
        Admin admin = conn.getAdmin();
        //创建表对象
        TableName tableName = TableName.valueOf("ns2:t2");
        //创建表描述符对象
        HTableDescriptor tbl = new HTableDescriptor(tableName);
        //创建列簇描述符
        HColumnDescriptor col = new HColumnDescriptor("f1");
        tbl.addFamily(col);
        admin.createTable(tbl);
    }

    /**
     * 禁用和删除表
     * HBase表一旦禁用就不可以执行put操作，会提示异常:NotServingRegionException
     * 也不可以执行get操作，会提示: xxx is disabled
     *
     * @throws Exception
     */
    @Test
    public void dropTable() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        //通过Hbase连接工厂创建连接
        Connection conn = ConnectionFactory.createConnection(conf);
        Admin admin = conn.getAdmin();
        TableName tableName = TableName.valueOf("ns2:t2");
        //禁用表
        admin.disableTable(tableName);
        //删除表
        admin.deleteTable(tableName);
    }

    /**
     * 删除数据
     *
     * @throws Exception
     */
    @Test
    public void deleteData() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
        HTable table = (HTable) conn.getTable(tname);
        Delete del = new Delete(Bytes.toBytes("row0001"));
        del.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("id"));
        del.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("name"));
        table.delete(del);
    }

    /**
     * 扫描指定列的数据
     *
     * @throws Exception
     */
    @Test
    public void scanByColumn() throws Exception{
        byte[] f1 = Bytes.toBytes("f1");
        byte[] name = Bytes.toBytes("name");
        byte[] id = Bytes.toBytes("id");
        byte[] age = Bytes.toBytes("age");

        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes("row5000"));
        scan.setStopRow(Bytes.toBytes("row8000"));
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            //hbase row
            Result r = it.next();
            byte[] nameValue = r.getValue(f1, name);
            System.out.println(Bytes.toString(nameValue));
        }
    }

    /**
     * 动态遍历1
     *
     * @throws Exception
     */
    @Test
    public void scanByFamilyMap() throws Exception{
        byte[] f1 = Bytes.toBytes("f1");
        byte[] name = Bytes.toBytes("name");
        byte[] id = Bytes.toBytes("id");
        byte[] age = Bytes.toBytes("age");

        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes("row5000"));
        scan.setStopRow(Bytes.toBytes("row8000"));
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            //hbase row
            Result r = it.next();
            Map<byte[], byte[]> map =  r.getFamilyMap(f1);
            for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
                String col = Bytes.toString(entry.getKey());
                String value = Bytes.toString(entry.getValue());
                System.out.println(col + ":" + value + ",");
            }
            System.out.println();
        }
    }

    /**
     * 动态遍历2
     *
     * @throws Exception
     */
    @Test
    public void scanByMap() throws Exception{
        byte[] f1 = Bytes.toBytes("f1");
        byte[] name = Bytes.toBytes("name");
        byte[] id = Bytes.toBytes("id");
        byte[] age = Bytes.toBytes("age");

        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes("row5000"));
        scan.setStopRow(Bytes.toBytes("row8000"));
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            //得到一行的所有map,key=f1,vaue=Map<Col,Map<Timestamp,value>>
            NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = r.getMap();
            //
            for (Map.Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> entry : map.entrySet()) {
                //获取列簇
                String family = Bytes.toString(entry.getKey());
                NavigableMap<byte[], NavigableMap<Long, byte[]>> colMap = entry.getValue();
                for (Map.Entry<byte[], NavigableMap<Long, byte[]>> ets : colMap.entrySet()) {
                    String col = Bytes.toString(ets.getKey());
                    Map<Long, byte[]> tsValueMap = ets.getValue();
                    for (Map.Entry<Long, byte[]> e : tsValueMap.entrySet()) {
                        Long ts = e.getKey();
                        String value = Bytes.toString(e.getValue());
                        System.out.print(family + ":" + col + ":" + ts + "=" + value + ",");
                    }
                }
            }
            System.out.println();
        }
    }

    /**
     * 按照版本数指定查询
     *
     * @throws Exception
     */
    @Test
    public void getWithVersions() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t3");
        Table table = conn.getTable(tname);
        Get get = new Get(Bytes.toBytes("row1"));

        //get.setMaxVersions();  //检索所有版本
        get.setMaxVersions(2);
        Result r = table.get(get);
        List<Cell> cells = r.getColumnCells(Bytes.toBytes("f1"), Bytes.toBytes("name"));
        for (Cell c : cells) {
            String f = Bytes.toString(c.getFamily());
            String col = Bytes.toString(c.getQualifier());
            long ts = c.getTimestamp();
            String val = Bytes.toString(c.getValue());
            System.out.println(f + "/" + col + "/" + ts + "=" + val);
        }
    }

    /**
     * 测试设置扫描器缓存之后，扫描数据的耗时
     * 测试结果
     * cache row num         cost time(ms)
     * 5000                  423
     * 1000                  612
     * 1                     7359
     *
     * @throws Exception
     */
    @Test
    public void scanCaching() throws Exception{
        byte[] f1 = Bytes.toBytes("f1");
        byte[] name = Bytes.toBytes("name");
        byte[] id = Bytes.toBytes("id");
        byte[] age = Bytes.toBytes("age");

        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        scan.setCaching(1000);
        ResultScanner rs = table.getScanner(scan);
        long start = System.currentTimeMillis();
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            //hbase row
            Result r = it.next();
            System.out.println(r.getColumnLatestCell(f1, name));
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    /**
     * 测试同时配置setCaching和setBatch之后 迭代数据的影响
     *
     * @throws Exception
     */
    @Test
    public void scanCachingAndBatch() throws Exception{
        byte[] f1 = Bytes.toBytes("f1");
        byte[] name = Bytes.toBytes("name");
        byte[] id = Bytes.toBytes("id");
        byte[] age = Bytes.toBytes("age");

        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        scan.setCaching(2);
        scan.setBatch(3);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            System.out.println("------------------------");
            Result r = it.next();
            //得到一行的所有map,key=f1,vaue=Map<Col,Map<Timestamp,value>>
            NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = r.getMap();
            //
            for (Map.Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> entry : map.entrySet()) {
                //获取列簇
                String family = Bytes.toString(entry.getKey());
                NavigableMap<byte[], NavigableMap<Long, byte[]>> colMap = entry.getValue();
                for (Map.Entry<byte[], NavigableMap<Long, byte[]>> ets : colMap.entrySet()) {
                    String col = Bytes.toString(ets.getKey());
                    Map<Long, byte[]> tsValueMap = ets.getValue();
                    for (Map.Entry<Long, byte[]> e : tsValueMap.entrySet()) {
                        Long ts = e.getKey();
                        String value = Bytes.toString(e.getValue());
                        System.out.print(family + ":" + col + "=" + value + ",");
                    }
                }
            }
            System.out.println();
        }
    }

    /**
     * RowFilter
     *
     * @throws Exception
     */
    @Test
    public void rowFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL,
                new BinaryComparator(Bytes.toBytes("row0100")));
        scan.setFilter(rowFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            System.out.println(Bytes.toString(r.getRow()));
        }
    }

    /**
     * FamilyFilter
     *
     * @throws Exception
     */
    @Test
    public void familyFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        FamilyFilter familyFilter = new FamilyFilter(CompareFilter.CompareOp.LESS_OR_EQUAL,
                new BinaryComparator(Bytes.toBytes("f2")));
        scan.setFilter(familyFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id));
        }
    }

    /**
     * QualifierFilter
     *
     * @throws Exception
     */
    @Test
    public void qualifierFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        QualifierFilter qualifierFilter = new QualifierFilter(CompareFilter.CompareOp.EQUAL,
                new BinaryComparator(Bytes.toBytes("id")));
        scan.setFilter(qualifierFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" +
                    Bytes.toString(f2_id) + " ; f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * DependentColumnFilter
     *
     * @throws Exception
     */
    @Test
    public void dependentColumnFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        DependentColumnFilter dependentColumnFilter = new DependentColumnFilter(
                Bytes.toBytes("f2"),
                Bytes.toBytes("addr"),
                true,
                CompareFilter.CompareOp.NOT_EQUAL,
                new BinaryComparator(Bytes.toBytes("beijing")));
        scan.setFilter(dependentColumnFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f1_name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id) +
                    " ; f1:name=" + Bytes.toString(f1_name) + " : f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * SingleColumnValueFilter
     * 如果value不满足，整行过滤掉
     *
     * @throws Exception
     */
    @Test
    public void singleColumnValueFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueExcludeFilter(
                Bytes.toBytes("f2"),
                Bytes.toBytes("name"),
                CompareFilter.CompareOp.NOT_EQUAL,
                new BinaryComparator(Bytes.toBytes("tom2.2")));
        scan.setFilter(singleColumnValueFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f1_name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id) +
                    " ; f1:name=" + Bytes.toString(f1_name) + " : f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * SingleColumnValueExcludeFilter
     * 去掉过滤使用的列，对列的值进行过滤
     *
     * @throws Exception
     */
    @Test
    public void singleColumnValueExcludeFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        SingleColumnValueExcludeFilter singleColumnValueExcludeFilter = new SingleColumnValueExcludeFilter(
                Bytes.toBytes("f2"),
                Bytes.toBytes("name"),
                CompareFilter.CompareOp.EQUAL,
                new BinaryComparator(Bytes.toBytes("tom2.2")));
        scan.setFilter(singleColumnValueExcludeFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f1_name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id) +
                    " ; f1:name=" + Bytes.toString(f1_name) + " : f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * 前缀过滤器,rowkey过滤器
     *
     * @throws Exception
     */
    @Test
    public void prefixFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        PrefixFilter prefixFilter = new PrefixFilter(Bytes.toBytes("row22"));
        scan.setFilter(prefixFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f1_name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id) +
                    " ; f1:name=" + Bytes.toString(f1_name) + " : f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * 分页过滤器,在Region上扫描额时，对每次page设置大小，返回到客户端时，会涉及到每个Region结果的合并
     *
     * @throws Exception
     */
    @Test
    public void pageFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        PageFilter prefixFilter = new PageFilter(10);
        scan.setFilter(prefixFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f1_name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id) +
                    " ; f1:name=" + Bytes.toString(f1_name) + " : f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * KeyOnly过滤器 丢弃Value,只提取Key
     *
     * @throws Exception
     */
    @Test
    public void keyOnlyFliter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        KeyOnlyFilter keyOnlyFilter = new KeyOnlyFilter();
        scan.setFilter(keyOnlyFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f1_name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id) +
                    " ; f1:name=" + Bytes.toString(f1_name) + " : f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * 列分页过滤器，过滤指定范围的列
     *
     * @throws Exception
     */
    @Test
    public void columnPaginationFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        ColumnPaginationFilter columnPaginationFilter = new ColumnPaginationFilter(2,2);
        scan.setFilter(columnPaginationFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f1_name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id) +
                    " ; f1:name=" + Bytes.toString(f1_name) + " : f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * 列分页过滤器，过滤指定范围的列
     *
     * @throws Exception
     */
    @Test
    public void likeRegaxFilter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
        ValueFilter valueFilter = new ValueFilter(
                CompareFilter.CompareOp.EQUAL,
                new RegexStringComparator("^tom2"));
        scan.setFilter(valueFilter);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f1_name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id) +
                    " ; f1:name=" + Bytes.toString(f1_name) + " : f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * 复杂过滤器(FilterList)
       实现如下查询:
       select *
         from ns1:t7
        where ((age <= 13) and (name like '%t'))
           or ((age > 13) and (name like 't%'));
     *
     * @throws Exception
     */
    @Test
    public void filterList() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t7");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();


        SingleColumnValueFilter ftl = new SingleColumnValueExcludeFilter(
                Bytes.toBytes("f2"),
                Bytes.toBytes("age"),
                CompareFilter.CompareOp.LESS_OR_EQUAL,
                new BinaryComparator(Bytes.toBytes("13")));

        SingleColumnValueFilter ftr = new SingleColumnValueExcludeFilter(
                Bytes.toBytes("f2"),
                Bytes.toBytes("name"),
                CompareFilter.CompareOp.EQUAL,
                new RegexStringComparator("t$"));

        FilterList ft = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        ft.addFilter(ftl);
        ft.addFilter(ftr);

        SingleColumnValueFilter fbl = new SingleColumnValueExcludeFilter(
                Bytes.toBytes("f2"),
                Bytes.toBytes("age"),
                CompareFilter.CompareOp.GREATER,
                new BinaryComparator(Bytes.toBytes("13")));

        SingleColumnValueFilter fbr = new SingleColumnValueExcludeFilter(
                Bytes.toBytes("f2"),
                Bytes.toBytes("name"),
                CompareFilter.CompareOp.EQUAL,
                new RegexStringComparator("^t"));

        FilterList fb = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        ft.addFilter(fbl);
        ft.addFilter(fbr);

        FilterList fall = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        ft.addFilter(ft);
        ft.addFilter(fb);

        scan.setFilter(fall);
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] f1_id = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
            byte[] f2_id = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("id"));
            byte[] f1_name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            byte[] f2_name = r.getValue(Bytes.toBytes("f2"), Bytes.toBytes("name"));
            System.out.println("f1:id=" + Bytes.toString(f1_id) + " ; f2:id=" + Bytes.toString(f2_id) +
                    " ; f1:name=" + Bytes.toString(f1_name) + " : f2:name=" + Bytes.toString(f2_name));
        }
    }

    /**
     * 计数器
     *
     * @throws Exception
     */
    @Test
    public void counter() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t8");
        Table table = conn.getTable(tname);
        Increment increment = new Increment(Bytes.toBytes("row1"));
        increment.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("daily"),1);
        increment.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("weekly"),10);
        increment.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("monthly"),100);
        table.increment(increment);
    }

}
