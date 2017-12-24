package com.it18zhang

import scala.io.Source

object FileDemo {
  def main(args: Array[String]): Unit = {
    val s = Source.fromFile("d:/hello.txt")
    val lines = s.getLines()
    for (line <- lines) {
      println(line)
    }

    val str = Source.fromFile("d:/hello.txt").mkString
    val iter = str.split("\\s+")
    for (it <- iter) {
      println(it)
    }

  }
}
