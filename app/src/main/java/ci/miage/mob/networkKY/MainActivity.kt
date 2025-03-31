package ci.miage.mob.networkKY


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import ci.miage.mob.NetworkKY.R
import ci.miage.mob.networkKY.models.Graph
import com.google.android.material.navigation.NavigationView
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Locale



class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var graphView: GraphView

    private val FILENAME = "saved_network.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        graphView = findViewById(R.id.graphView)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.reset_network -> {
                    resetNetwork()
                    true
                }
                R.id.save_network -> {
                    saveNetwork()
                    true
                }
                R.id.show_saved_network -> {
                    showSavedNetwork()
                    true
                }
                R.id.objets -> {
                    graphView.setMode(GraphView.GraphMode.OBJECTS)
                    Toast.makeText(this, getString(R.string.mode_objects_enabled), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.connexions -> {
                    graphView.setMode(GraphView.GraphMode.CONNECTIONS)
                    Toast.makeText(this, getString(R.string.mode_connections_enabled), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.objets_connexions -> {
                    graphView.setMode(GraphView.GraphMode.OBJECTS_CONNECTIONS)
                    Toast.makeText(this, getString(R.string.mode_objects_connections_enabled), Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.choose_french -> {
                    setAppLocale(this, "fr")
                    recreate()
                    true
                }

                R.id.choose_english -> {
                    setAppLocale(this, "en")
                    recreate()
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    fun setAppLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun resetNetwork() {
        graphView.graph =  Graph(mutableListOf(), mutableListOf())
    }

    /**
     * Sauvegarde l'état du réseau dans la mémoire interne
     */
    private fun saveNetwork() {
        try {
            saveGraphToInternalStorage(this, graphView.graph, "network_graph.json")

            Toast.makeText(this, "Réseau sauvegardé", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Affiche le réseau sauvegardé
     */
    private fun showSavedNetwork() {
        try {
            val loadedGraph = loadGraphFromInternalStorage(this, "network_graph.json")
            if (loadedGraph != null) {
                graphView.graph = loadedGraph
            } else {
                Toast.makeText(this, "Aucune sauvegarde trouvée", Toast.LENGTH_SHORT).show()
            }


        } catch (e: FileNotFoundException) {
            Toast.makeText(this, "Aucune sauvegarde trouvée", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur lors de la lecture", Toast.LENGTH_SHORT).show()
        }
    }


    fun saveGraphToInternalStorage(context: Context, graph: Graph, fileName: String) {
        try {
            // Convert the Graph object to JSON
            val jsonString = Json.encodeToString(graph)

            // Open a file output stream to write the data
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()  // Log or handle the error
        }
    }

    fun loadGraphFromInternalStorage(context: Context, fileName: String): Graph? {
        return try {
            // Open the file to read
            val file = File(context.filesDir, fileName)
            val fileInputStream = FileInputStream(file)

            // Read the JSON data from the file and convert it back to the Graph object
            val jsonString = fileInputStream.bufferedReader().use { it.readText() }

            // Deserialize the JSON string to a Graph object
            Json.decodeFromString<Graph>(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()  // Log or handle the error
            null
        }
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
