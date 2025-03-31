package ci.miage.mob.networkKY.models

import kotlinx.serialization.Serializable

@Serializable
data class Node(
    var x: Float,
    var y: Float,
    var label: String = "",
    var color: Int = 0, // Noir par défaut
    var icon: String = "default" // Valeur par défaut null pour l'icône
)