package ci.miage.mob.networkKY


import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import ci.miage.mob.NetworkKY.R
import ci.miage.mob.networkKY.models.Edge
import ci.miage.mob.networkKY.models.Graph
import ci.miage.mob.networkKY.models.Node
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.sqrt


class GraphView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    public var graph: Graph = Graph(mutableListOf(), mutableListOf())
    private val paint = Paint()
    private var selectedNode: Node? = null
    private var selectedEdge: Edge? = null
    private var touchOffsetX = 0f
    private var touchOffsetY = 0f
    private var tempEdgeEndX = 0f
    private var tempEdgeEndY = 0f
    private var longPressHandler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null
    private var longPressX = 0f
    private var longPressY = 0f

    val COLOR = intArrayOf(
        Color.BLACK,
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.CYAN,
        Color.MAGENTA,

        Color.parseColor("#FFA500") // Orange
    )


    val NODE_ICONS: Map<String, Drawable?> = mapOf(
        "default" to ContextCompat.getDrawable(context, R.drawable.circleo),
        "printer" to ContextCompat.getDrawable(context, R.drawable.printer),
        "tv" to ContextCompat.getDrawable(context, R.drawable.tv),
        "lamp" to ContextCompat.getDrawable(context, R.drawable.light),
        "computer" to ContextCompat.getDrawable(context, R.drawable.computer),
        "router" to ContextCompat.getDrawable(context, R.drawable.router),
        "speaker" to ContextCompat.getDrawable(context, R.drawable.speaker),
        "kitchen" to ContextCompat.getDrawable(context, R.drawable.kitchen)
    )



    enum class GraphMode { OBJECTS, CONNECTIONS, OBJECTS_CONNECTIONS }
    private var mode: GraphMode = GraphMode.OBJECTS

    fun setMode(newMode: GraphMode) {
        mode = newMode
    }

    init {
        paint.color = ContextCompat.getColor(context, android.R.color.black)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.isAntiAlias = true
    }

    fun drawEdgeLabel(canvas: Canvas, edge: Edge) {
        val mid = edge.getMidPoint()
        val textPaint = Paint().apply {
            color = COLOR[edge.color]
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(edge.label, mid.x+5, mid.y, textPaint)
    }

    fun drawNodeWithIcon(canvas: Canvas, node: Node) {
        val icon = NODE_ICONS[node.icon] ?: return
        val nodeRadius = 20f
        // Dessiner l'icône au centre du nœud
        val iconSize = (nodeRadius * 2).toInt()
        val left = (node.x - nodeRadius).toInt()
        val top = (node.y - nodeRadius).toInt()
        val right = left + iconSize
        val bottom = top + iconSize

        icon.setColorFilter(COLOR[node.color], PorterDuff.Mode.SRC_IN)
        icon.setBounds(left, top, right, bottom)
        icon.draw(canvas)


        // Dessiner l'étiquette du nœud
        paint.color = COLOR[node.color]
        paint.textSize = 30f
        canvas.drawText(node.label, node.x + nodeRadius + 5, node.y + 5, paint)
        paint.style = Paint.Style.FILL
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val nodeRadius = 20f

        for (edge in graph.edges) {
            val linePaint = Paint().apply {
                color = COLOR[edge.color]
                strokeWidth = edge.thinkness
                style = Paint.Style.STROKE
            }
            val path = Path().apply {
                moveTo(edge.source.x, edge.source.y)
                if(edge.curvaturePoint==null){

                    lineTo(edge.destination.x, edge.destination.y)
                }else{
                  quadTo(
                    edge.curvaturePoint!!.x,
                    edge.curvaturePoint!!.y,
                    edge.destination.x,
                    edge.destination.y
                )

                }


            }
            canvas.drawPath(path, linePaint)

            // Dessin de l'étiquette
            drawEdgeLabel(canvas, edge)
        }

        if (selectedNode != null && mode == GraphMode.CONNECTIONS) {
            paint.color = Color.GRAY
            canvas.drawLine(selectedNode!!.x, selectedNode!!.y, tempEdgeEndX, tempEdgeEndY, paint)
            paint.color = ContextCompat.getColor(context, android.R.color.black)
        }

        for (node in graph.nodes) {
            drawNodeWithIcon(canvas, node)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedNode = findNodeAt(x, y)
                selectedEdge= findEdgeNearMidpoint(x, y)

                if (selectedNode == null && (mode == GraphMode.OBJECTS)) {
                    longPressX = x
                    longPressY = y
                    longPressRunnable = Runnable {
                        showNodeCreationDialog(longPressX, longPressY)
                    }
                    longPressHandler.postDelayed(longPressRunnable!!, 500)
                } else if (selectedNode != null && (mode == GraphMode.CONNECTIONS)) {
                    tempEdgeEndX = x
                    tempEdgeEndY = y


                }else if (selectedNode!=null && mode == GraphMode.OBJECTS_CONNECTIONS){

                    longPressRunnable = Runnable {
                        showNodeEditDialog(selectedNode!!)
                    }
                    longPressHandler.postDelayed(longPressRunnable!!, 500)

                }
                else if (selectedEdge!=null && mode == GraphMode.OBJECTS_CONNECTIONS){
                    longPressRunnable = Runnable {
                        showEdgeEditDialog(selectedEdge!!)
                    }
                    longPressHandler.postDelayed(longPressRunnable!!, 500)

                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (selectedNode != null) {
                    if (mode == GraphMode.CONNECTIONS) {
                        tempEdgeEndX = x
                        tempEdgeEndY = y
                    }else if (mode==GraphMode.OBJECTS){
                        selectedNode!!.x=x
                        selectedNode!!.y=y
                    }
                }
                if (selectedEdge!=null){
                    if (mode==GraphMode.CONNECTIONS){
                        selectedEdge!!.curvaturePoint = PointF(x,y)
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                longPressHandler.removeCallbacks(longPressRunnable!!)

                if (selectedNode != null && mode == GraphMode.CONNECTIONS) {
                    val targetNode = findNodeAt(x, y)
                    if (targetNode != null && targetNode != selectedNode) {
                        val edgeExists = graph.edges.any {

                            (it.source.label == selectedNode!!.label && it.destination.label == targetNode.label) ||
                                    (it.source.label == targetNode.label && it.destination.label == selectedNode!!.label)
                        }

                        if (!edgeExists) {
                            val edge = addEdge(selectedNode!!.label, targetNode.label)
                            edge?.let { showEdgeCreationDialog(it) }
                        }
                    }
                }

                selectedEdge=null
                selectedNode = null
                invalidate()
            }
        }
        return true
    }

    private fun findEdgeNearMidpoint(x: Float, y: Float): Edge? {
        val threshold = 50f // The maximum distance to consider for a "near" tap
        for (edge in graph.edges) {
            val midPoint = edge.getMidPoint()
            val distance = sqrt(((x - midPoint.x).pow(2) + (y - midPoint.y).pow(2)).toDouble()).toFloat()
            if (distance <= threshold) {
                return edge
            }
        }
        return null
    }

    private fun findNodeAt(x: Float, y: Float): Node? {
        val nodeRadius = 20f
        return graph.nodes.find {
            val distance = sqrt(((x - it.x).pow(2) + (y - it.y).pow(2)).toDouble()).toFloat()
            distance <= nodeRadius
        }
    }

    fun addNode(x: Float, y: Float, label: String) {
        graph.nodes.add(Node(x, y, label))
        invalidate()
    }

    fun addEdge(label1: String, label2: String): Edge? {
        val node1 = graph.nodes.find { it.label == label1 }
        val node2 = graph.nodes.find { it.label == label2 }
        if (node1 != null && node2 != null) {
            var edge = Edge(node1, node2)
            return edge
        }

        return null
    }

    private fun showTextInputDialog(title: String, hint: String, defaultText: String = "", onConfirm: (String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val input = EditText(context)
        input.hint = hint
        input.setText(defaultText)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                onConfirm(text)
            } else {
                Toast.makeText(context, "Le champ ne peut pas être vide", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Annuler") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showNodeCreationDialog(x: Float, y: Float) {
        showTextInputDialog("Créer un nœud", "Nom du nœud") { nodeName ->
            addNode(x, y, nodeName)
        }
    }

    private fun showEditLabelDialog(node: Node) {
        showTextInputDialog("Modifier l'étiquette", "Nouvelle étiquette", node.label) { newLabel ->
            node.label = newLabel
            invalidate()
        }
    }


    private fun showEdgeCreationDialog(edge: Edge) {
        showTextInputDialog("Créer une connexion", "Nom:", edge.label) { label ->
            edge.label = label
            graph.edges.add(edge)
            invalidate()
        }
    }

    private fun showEdgeEditLabelDialog(edge: Edge) {
        showTextInputDialog("modifier la connexion", "Nom:", edge.label) { label ->
            edge.label = label

            invalidate()
        }
    }



    private fun showColorPickerDialog(target: Any) {
        val colorNames = context.resources.getStringArray(R.array.color_names)
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.choose_color))

        builder.setItems(colorNames) { _, which ->
            when (target) {
                is Node -> target.color = which
                is Edge -> target.color = which
            }
            invalidate()
        }

        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }





    private fun showNodeEditDialog(node: Node) {
        val options = arrayOf(
            context.getString(R.string.edit_label),
            context.getString(R.string.change_color),
            context.getString(R.string.replace_icon),
            context.getString(R.string.delete_node)
        )

        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.edit_node))
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showEditLabelDialog(node)
                1 -> showColorPickerDialog(node)
                2 -> showIconSelectionDialog(node)
                3 -> {
                    graph.nodes.remove(node)
                    invalidate()
                }
            }
        }

        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }


    private fun showEdgeEditDialog(edge: Edge) {
        val options = arrayOf(
            context.getString(R.string.edit_label),
            context.getString(R.string.change_color),
            context.getString(R.string.change_thickness),
            context.getString(R.string.delete_connection)
        )

        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.edit_connection))
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showEdgeEditLabelDialog(edge)
                1 -> showColorPickerDialog(edge)
                2 -> showThicknessPickerDialog(edge)
                3 -> {
                    graph.edges.remove(edge)
                    invalidate()
                }
            }
        }

        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showIconSelectionDialog(node: Node) {
        val iconNames = NODE_ICONS.keys.toTypedArray()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choisir une icône")
        builder.setItems(iconNames) { _, which ->
            val selectedIcon = NODE_ICONS[iconNames[which]]
            if (selectedIcon != null) {
                node.icon = iconNames[which]
                invalidate()
            }
        }

        builder.setNegativeButton("Annuler") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showThicknessPickerDialog(edge: Edge) {
        // Define the thickness options as strings for display
        val thicknessOptions = arrayOf("4px", "6px", "8px", "9px", "10px")
        // Define the corresponding thickness values (as floats) to apply to the edge's stroke width
        val thicknessValues = floatArrayOf(4f, 6f, 8f, 9f, 10f)

        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.change_thickness))
        builder.setItems(thicknessOptions) { _, which ->
            // Set the selected thickness to the edge's stroke width
            edge.thinkness = thicknessValues[which]
            invalidate()  // Redraw the graph to reflect the updated thickness
        }

        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

}




