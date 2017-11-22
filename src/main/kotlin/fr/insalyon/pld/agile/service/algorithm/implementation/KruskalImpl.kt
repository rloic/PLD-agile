package fr.insalyon.pld.agile.service.algorithm.implementation

import fr.insalyon.pld.agile.service.algorithm.api.Kruskal

typealias Node = Int
typealias Length = Int
typealias Set = BooleanArray

class KruskalImpl(
    val nodes: ArrayList<Node>,
    edges: Array<out IntArray>,
    val noEdgeValue: Int = Int.MAX_VALUE
) : Kruskal {

  val edges = List<List<Triple<Node, Length, Node>>>(nodes.size, { departure ->
    val outEdges = mutableListOf<Triple<Node, Length, Node>>()
    nodes
        .filter { destination -> departure != destination && edges[departure][destination] != noEdgeValue }
        .forEach { destionation -> outEdges += Triple(departure, edges[departure][destionation], destionation) }
    outEdges
  })

  private val parent = Array<Node?>(nodes.size, { _ -> null})

  private fun find(x: Node): Node {
    return if(parent[x] == null) {
      x
    } else {
      find(parent[x]!!)
    }
  }

  private fun union(x: Node, y: Node) {

    val xRoot = find(x)
    val yRoot = find(y)

    if(xRoot != yRoot) {
      parent[xRoot] = yRoot
    }

  }

  override fun getLength(): Int {

    val set = Set(nodes.size, { _ -> false})
    // Sort all the edges by length (it.second)
    val edges = edges.flatten().sortedBy { it.second }.iterator()
    var length = 0

    // Create subset is already done

    edges.forEach { edge ->
      if(find(edge.first) != find(edge.third)) {
        length += edge.second
        union(edge.first, edge.third)
      }
    }

    return length

  }

  fun Set.isNotFull() = any { !it }

  fun Set.add(node: Node) {
    this[node] = true
  }

  operator fun Set.contains(node: Node): Boolean {
    return this[node]
  }

}