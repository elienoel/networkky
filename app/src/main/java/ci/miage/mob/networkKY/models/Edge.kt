package ci.miage.mob.networkKY.models

import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF
import kotlinx.serialization.Serializable

@Serializable
data class Edge(
    val source: Node,
    val destination: Node,
    var label: String = "",
    var thinkness: Float = 5f,
    var color: Int = 0, // Noir par défaut
    var curvaturePoint: PointF? = null

){
    // Calcul du point milieu sur la courbe
    fun getMidPoint(): PointF {
        val path = Path().apply {
            moveTo(source.x, source.y)
            if (curvaturePoint==null){
                lineTo(destination.x, destination.y)
            }else{
                quadTo(curvaturePoint!!.x, curvaturePoint!!.y, destination.x, destination.y)
            }

        }
        val measure = PathMeasure(path, false)
        val coords = FloatArray(2)
        measure.getPosTan(measure.length * 0.5f, coords, null)
        return PointF(coords[0], coords[1])
    }
}
