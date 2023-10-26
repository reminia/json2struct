package json2struct

// a simple edge list Graph
case class Graph[A](nodes: Set[A], edges: Set[(A, A)]) {
  def indegree(a: A): Int = edges.count(_._2 == a)

  def outdegree(a: A): Int = edges.count(_._1 == a)

  def sources: Set[A] = nodes.filter(x => indegree(x) == 0)

}