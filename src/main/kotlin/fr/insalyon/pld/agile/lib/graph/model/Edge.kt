package fr.insalyon.pld.agile.lib.graph.model

/**
 * An edge from the node `from` to the node `to` using the link `element`
 */
class Edge<N, out E : Measurable>(
    val from: Node<N>,
    val element: E,
    val to: Node<N>
) {
  override fun toString(): String = "Edge(from=${from.index}, to=${to.index}, element=$element)"
}