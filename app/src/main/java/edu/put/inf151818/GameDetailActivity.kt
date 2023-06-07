package edu.put.inf151818

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class GameDetails(
    var Id: String? = null,
    var title: String? = null,
    var description: String? = null,
    var yearPublished: String? = null,
    var minPlayers: String? = null,
    var maxPlayers: String? = null,
    var thumbnailUrl: String? = null
)

class GameDetailActivity : AppCompatActivity() {
    private lateinit var idTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var descriptionTextView: TextView
    private lateinit var yearPublishedTextView: TextView
    private lateinit var minPlayersTextView: TextView
    private lateinit var maxPlayersTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)


        titleTextView = findViewById(R.id.titleTextView)
        imageView = findViewById(R.id.imageView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        yearPublishedTextView = findViewById(R.id.yearPublishedTextView)
        minPlayersTextView = findViewById(R.id.minPlayersTextView)
        maxPlayersTextView = findViewById(R.id.maxPlayersTextView)
        idTextView = findViewById(R.id.idTextView)

        val intent = intent
        val gameId = intent.getStringExtra("gameId")

        retrieveGameDetails(gameId)
    }

    private fun retrieveGameDetails(gameId: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = "https://boardgamegeek.com/xmlapi2/thing?id=$gameId"

            try {
                val xmlStream = downloadXml(url)
                val gameDetails = parseGameDetails(xmlStream)
                gameDetails.Id = gameId
                displayGameDetails(gameDetails)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun downloadXml(urlString: String): InputStream {
        val url = URL(urlString)
        val connection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
        connection.connect()
        return connection.inputStream
    }

    private fun parseGameDetails(xmlStream: InputStream): GameDetails {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(xmlStream, null)

        var eventType = parser.eventType
        var gameDetails = GameDetails()
        var primaryName: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name == "name" && parser.getAttributeValue(null, "type") == "primary") {
                    primaryName = parser.getAttributeValue(null, "value")
                    gameDetails.title = primaryName
                }
                when (parser.name) {
                    "thumbnail" -> gameDetails.thumbnailUrl = parser.nextText()
                    "description" -> gameDetails.description = parser.nextText()
                    "yearpublished" -> gameDetails.yearPublished = parser.getAttributeValue(null, "value")
                    "minplayers" -> gameDetails.minPlayers = parser.getAttributeValue(null, "value")
                    "maxplayers" -> gameDetails.maxPlayers = parser.getAttributeValue(null, "value")
                }
            }
            eventType = parser.next()
        }
        return gameDetails
    }

    private suspend fun displayGameDetails(gameDetails: GameDetails) {
        withContext(Dispatchers.Main) {
            titleTextView.text = "Title: " + gameDetails.title
            yearPublishedTextView.text = gameDetails.yearPublished
            minPlayersTextView.text = gameDetails.minPlayers
            maxPlayersTextView.text = gameDetails.maxPlayers
            idTextView.text = "Game id: " + gameDetails.Id

            // Extract the first three sentences from the description
            val descriptionSentences = gameDetails.description?.split(".")
            val truncatedDescription = descriptionSentences?.take(3)?.joinToString(". ")

            descriptionTextView.text = truncatedDescription

            val bitmap = downloadThumbnail(gameDetails.thumbnailUrl)
            imageView.setImageBitmap(bitmap)
        }
    }


    private suspend fun downloadThumbnail(urlString: String?): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpsURLConnection
                connection.connect()
                val inputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
