package dbms.datastructures

import scala.annotation.tailrec
import java.util.NoSuchElementException
import UnbalancedSearchTree.*
import math.Ordering.Implicits.infixOrderingOps
import math.Ordered.orderingToOrdered



object UnbalancedSearchTree {
  private final case class Node[K, V](
    var key: K,
    var value: V,
    var left: Option[Node[K, V]],
    var right: Option[Node[K, V]]
  )

  private def leaf[K, V](key: K, value: V): Node[K, V] = Node(key, value, None, None)

  def apply[K, V](elements: (K, V)*)(ord: Ordering[K]): UnbalancedSearchTree[K, V] = {
    val set = new UnbalancedSearchTree[K, V](ord)
    elements.foreach(set.addOrUpdate)
    set
  }
}


class UnbalancedSearchTree[K, V](ord: Ordering[K]) {

  private var root: Option[Node[K, V]] = None
  private var counter: Int = 0

  def size: Int = counter

  def clear(): Unit = {
    root = None
    counter = 0
  }

  def addOrUpdate(key: K, value: V): Unit = {
    val newNode: Node[K, V] = Node(key, value, None, None)
    this.root match {
      case None => this.root = Some(newNode)
      case _ => if this.find(key).isEmpty then {
        this.add(key, value)
        counter += 1
      } else this.find(key).get.value = value
    }
  }


  private def find(key: K): Option[Node[K, V]] = {
    var tree = this
    tree.root.getOrElse(None) match {
      //empty tree
      case None => None
      //node found
      // warum key als Any und nicht key, meinen parameter??
      case Node(`key`,_,_,_) => root
      //node not found
      case Node(_,_,node1: Some[Node[K,V]], node2: Some[Node[K,V]]) => {
        if ord.lt(tree.root.get.key, key) then tree.root = node2
        else tree.root = node1
        tree.find(key)
      }
      case Node(_,_,None,node: Some[Node[K,V]]) =>
        if ord.lt(tree.root.get.key, key) then {
          node.get.right = tree.root
          tree.find(key)
        }
        else None
      case Node(_,_,node: Some[Node[K,V]], None) =>
        if ord.lt(tree.root.get.key, key) then None
        else {
          node.get.left = tree.root
          tree.find(key)
        }

      case Node(_,_,None, None) => None

    }
  }

  private def add(key: K, value: V): Unit = {
    val tree = this
    if tree.root.isEmpty then {
      tree.root = Some(Node(key, value, None, None))
    }
    else {
      // rechten Kinds-Knoten prüfen && ggf. ersetzen
      if ord.lt(tree.root.get.key, key) then {
        if tree.root.get.right.isEmpty then tree.root.get.right = Some(leaf(key, value))
        else {
          tree.root = tree.root.get.right
          tree.add(key, value)
        }
      }
      //linken Kindsknoten prüfen && ggf. ersetzen
      else {
        if tree.root.get.left.isEmpty then tree.root.get.left = Some(leaf(key, value))
        else {
          tree.root = tree.root.get.left
          tree.add(key, value)
        }
      }
    }
  }


  def get(key: K): Option[V] = {
    val found = find(key)
    if found.isEmpty then None
    else Some(found.get.value)
  }

  def apply(key: K): V = find(key).getOrElse(throw NoSuchElementException("this key does not exist in this tree")).value


  // bonus assignment (enable the tests yourself!)
  def getRange(inclusiveLowerBound: K, exclusiveUpperBound: K): Seq[(K, V)] = ???

}
