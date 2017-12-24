package com.it18zhang

import scala.collection.mutable


object CollDemo {
  def main(args: Array[String]): Unit = {
    import scala.collection.mutable._

    /**
      * List
      */
    val list = LinkedList(1,2,3,4)
    var tmp = list
    while (tmp.next != Nil) {
      tmp = tmp.next
      println(tmp)
    }

    /**
      * Set
      */
    val set = mutable.Set(1,2,3)
    set.add(2)
    for (e <- set) {
      println(e)
    }
  }
}
