package com.it18zhang.app.calllog;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Hbase二次索引
 */
public class CalleeLogRegionObserver extends BaseRegionObserver{

    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e,
                        Put put, WALEdit edit, Durability durability) throws IOException {
        super.postPut(e, put, edit, durability);

        //
        String callLogs = TableName.valueOf("ns1:calllogs").getNameAsString();

        //
        String tableName = e.getEnvironment().getRegion().getRegionInfo().getTable().getNameAsString();

        if (!callLogs.equals(tableName)) {
            return;
        }

        //获取主叫的rowkey
        String rowkey = Bytes.toString(put.getRow());
        //calllogs-rowkey: xx,callerid,calltime,direction,calleeid,duration

        String[] arr = rowkey.split(",");

        if(arr[3].equals("1")) {
            return;
        }

        //区域00~99
        String hash = Util.getRegionNo(arr[4], arr[2], 100);

        String newRowKey = hash + "," + arr[4] + "," + arr[2] + ",1," + arr[1] + "," +  arr[5];

        Put newPut = new Put(Bytes.toBytes(newRowKey));
        newPut.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("dummy"), Bytes.toBytes("no"));

        Table t = e.getEnvironment().getTable(TableName.valueOf("ns1:calllogs"));
        t.put(newPut);
    }

}
