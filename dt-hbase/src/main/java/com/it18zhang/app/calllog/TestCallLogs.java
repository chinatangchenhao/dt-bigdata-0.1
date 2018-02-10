package com.it18zhang.app.calllog;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class TestCallLogs {
    /**
     * ns1:calllogs主叫表中put数据
     *
     * rowkey设计如下：
     * 区域编码,主叫或者被叫,通话时间,方向(0-主,1-被),对方号码,通话时长
     * xx,callerid,calltime,direction,calleeid,duration
     *
     * @throws Exception
     */
    @Test
    public void putCallLogs() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:calllogs");
        Table table = conn.getTable(tname);

        String callerId = "13845456767";
        String calleeId = "13989897878";

        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern("yyyyMMddHHmmss");
        String callTime = format.format(new Date());

        int duration = 100;
        int regionNo = 100;
        //区域00~99
        int hash = (callerId + callTime.substring(0,6)).hashCode();
        hash = (hash & Integer.MAX_VALUE) % regionNo;

        DecimalFormat df = new DecimalFormat();
        df.applyPattern("00");
        //hash区域号
        String regNo = df.format(hash);

        DecimalFormat dff = new DecimalFormat();
        dff.applyPattern("00000");
        String durStr = dff.format(duration);

        //rowkey
        String rowkey = regNo + "," + callerId + "," + callTime + ",0," + calleeId + "," + durStr;

        byte[] rowid = Bytes.toBytes(rowkey);
        Put put = new Put(rowid);

        put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("callerPos"), Bytes.toBytes("heibei"));
        put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("calleePos"), Bytes.toBytes("heinan"));

        //插入数据
        table.put(put);
    }

    /**
     * 打印通话记录
     *
     * @throws Exception
     */
    @Test
    public void selectCallLogs() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:calllogs");
        Table table = conn.getTable(tname);

        String callerId = "13845456767";
        String month = "201703";

        String regNo = Util.getRegionNo(callerId, month, 100);

        String startKey = regNo + "," + callerId + "," + month;
        String stopKey = regNo + "," + callerId + ",201704";

        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes(startKey));
        scan.setStopRow(Bytes.toBytes(stopKey));
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            String row = Bytes.toString(it.next().getRow());
            System.out.println(row);
        }
    }
}
