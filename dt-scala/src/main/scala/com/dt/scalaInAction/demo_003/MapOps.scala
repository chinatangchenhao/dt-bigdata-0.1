package com.dt.scalaInAction.demo_003

/**
 * Map
 */
object MapOps {
  def main(args: Array[String]): Unit = {
    var entry = Map("John"->21,"Bell"->24)  //Map的静态定义    var entry: Map[String, Int]

    /**
     * 遍历k v  采用Tuple的方式遍历
     */
    for((k, v) <- entry) println("key="+k+";value="+v)
    
    /**
     * 遍历k 忽略v
     */
    for((k, _) <- entry) println("key="+k)   //(k, _) _为占位符
    
  }
}