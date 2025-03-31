package ci.miage.mob.networkKY.models

import kotlinx.serialization.Serializable

@Serializable
data class Graph(
    val nodes: MutableList<Node> = mutableListOf(),
    val edges: MutableList<Edge> = mutableListOf()
)
