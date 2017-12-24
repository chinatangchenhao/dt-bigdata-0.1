package com.it18zhang

import java.util.regex.Pattern

import scala.io.Source

/**
  * 正则href解析
  */
class RegaxParserDemo {
  def main(args: Array[String]): Unit = {
    val str = Source.fromFile("d:/1.html").mkString
    val p = Pattern.compile("<a\\s*href=\"([\u0000-\uffff&&[^\u005c\u0022]]*)\"")
    val m = p.matcher(str)
    while(m.find) {
      val s = m.group(1)
    }
  }
}
