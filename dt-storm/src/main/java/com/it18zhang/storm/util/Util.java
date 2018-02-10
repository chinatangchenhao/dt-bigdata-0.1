package com.it18zhang.storm.util;

import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

public class Util {

    /**
     * 获取主机名
     *
     * @return
     */
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回进程PID
     *
     * @return
     */
    public static String getPID() {
        String info = ManagementFactory.getRuntimeMXBean().getName();
        return info.split("@")[0];
    }

    /**
     * 返回线程PID
     *
     * @return
     */
    public static String getTID() {
        return Thread.currentThread().getName();
    }

    /**
     * 获取对象信息
     *
     * @param obj
     * @return
     */
    public static String getOID(Object obj) {
        String cname = obj.getClass().getSimpleName();
        int hash = obj.hashCode();
        return cname + "@" + hash;
    }

    public static String info(Object obj, String msg) {
         return getHostname() + "," + getPID() + "," + getTID() + "," + getOID(obj) + ":" + msg;
    }

    /**
     * 向服务端发送套接字消息
     *
     * @param obj
     * @param msg
     */
    public static void sendToClient(Object obj, String msg, int port) {
        try {
            Socket socket = new Socket("s201", port);
            OutputStream os = socket.getOutputStream();
            os.write((info(obj, msg)+"\r\n").getBytes());
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向服务端发送套接字消息
     *
     * @param obj
     * @param msg
     */
    public static void sendToLocalhost(Object obj, String msg) {
        try {
            Socket socket = new Socket("localhost", 8888);
            OutputStream os = socket.getOutputStream();
            os.write((info(obj, msg)+"\r\n").getBytes());
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
