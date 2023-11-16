package json2struct

import json2struct.GoStructAST.{Field, Struct}
import json2struct.GoType.GoArray

// a simple edge list Graph
case class Graph[A](nodes: Set[A], edges: Set[(A, A)]) {
  def indegree(a: A): Int = edges.count(_._2 == a)

  def sources: Set[A] = nodes.filter(x => indegree(x) == 0)
}

object Graph {
  def from(ss: Seq[Struct]): Graph[String] = {
    val nodes = Set.newBuilder[String]
    val edges = Set.newBuilder[(String, String)]
    ss.foreach { s =>
      nodes += s.name
      s.fields.foreach {
        case f: Field.Struct =>
          edges += (s.name -> f.name)
          nodes += s.name += f.name
        case Field.Simple(_, GoArray(tpe), _) if tpe.isStruct =>
          edges += s.name -> tpe.desc
          nodes += s.name += tpe.desc
        case _ => ()
      }
    }
    Graph(nodes.result(), edges.result())
  }
}