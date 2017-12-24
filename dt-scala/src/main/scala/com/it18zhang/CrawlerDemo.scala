package com.it18zhang

import java.io.FileOutputStream
import java.net.URL

/**
  * scala实现简单网页爬虫
  */
object CrawlerDemo {
  def main(args: Array[String]): Unit = {
    val url = new URL("http://www.kokojia.com/Public/images/upload/article/2016-08/57aacf0362bc6.png")
    val conn = url.openConnection()
    val in = conn.getInputStream
    val out = new FileOutputStream("d:/1.jpg")
    //创建字节数组
    var len = 0
    val buf = new Array[Byte](1024)
    while((len = in.read(buf)) != -1) {
      out.write(buf,0,len)
    }
    out.close
    in.close
  }
}
