package com.dt.scalaInAction.demo_003

/**
 * 元祖
 */
object TupleOps {
  
  def main(args: Array[String]): Unit = {
    var pair = (100, "Scala", "Spark")  //注意 鼠标上移变量pair 显示var pair: (Int, String, String) 这是Scala的类型推断
//    println(pair._0); //注意:去元素是下标从1开始  ._0 会编译错误
    println(pair._1)
    println(pair._2)
    println(pair._3)
  }
  
}