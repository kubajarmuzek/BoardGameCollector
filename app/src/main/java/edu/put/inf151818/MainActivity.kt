package edu.put.inf151818

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.io.StringReader
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import android.content.Intent
import edu.put.inf151818.ProfileActivity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import DatabaseHelper
import android.content.ContentValues

data class Game(
    var title: String? = null,
    var originalTitle: String? = null,
    var year: Int? = null,
    var bggId: Long? = null,
    var thumbnail: String? = null
)


class MainActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var button: Button
    private lateinit var textView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        button = findViewById(R.id.button)
        textView = findViewById(R.id.textView)

        sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val dateStr = sharedPreferences.getString("currentDateStr", "")

        if (dateStr != null) {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDate = Date() // Get the current date and time
            val currentDateStr = format.format(currentDate)

            try {
                val lastSyncDate = format.parse(dateStr)
                val diffInMilliseconds = currentDate.time - lastSyncDate.time
                val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMilliseconds)

                if (diffInHours < 24) {
                    textView.text = "Time difference is less than 24 hours"
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    finish() // Add this line to prevent going back to MainActivity when pressing the back button
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        button.setOnClickListener {
            val username = editText.text.toString()
            FetchDataTask().execute(username)
        }
    }



    private inner class FetchDataTask : AsyncTask<String, Void, String>() {
        private var username: String? = null
        private var totalItems: String? = null
        override fun doInBackground(vararg params: String?): String {
            username = params.getOrNull(0)

            val url = URL("https://www.boardgamegeek.com/xmlapi2/collection?username=$username")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                bufferedReader.close()
                return response.toString()
            } else {
                Log.e("FetchDataTask", "Error: $responseCode")
            }

            return ""
        }

        override fun onPostExecute(result: String?) {
            Log.d("FetchDataTask", "Response: $result")

            totalItems = result ?: ""

            if (username != null) {
                val xmlPullParserFactory = XmlPullParserFactory.newInstance()
                val xmlPullParser = xmlPullParserFactory.newPullParser()

                xmlPullParser.setInput(StringReader(result))

                var eventType = xmlPullParser.eventType
                var totalItems: Int? = null
                var expansionCount = 0

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (xmlPullParser.name == "items") {
                                val totalItemsString = xmlPullParser.getAttributeValue(null, "totalitems")
                                totalItems = totalItemsString?.toIntOrNull()
                            } else if (xmlPullParser.name == "item" && xmlPullParser.getAttributeValue(null, "subtype") == "boardgameexpansion") {
                                expansionCount++
                            }
                        }
                    }

                    eventType = xmlPullParser.next()
                }
                totalItems = totalItems?.minus(expansionCount) ?: 0

                if (username != null) {
                    // ... Parsing XML and obtaining game information ...

                    val gameList = ArrayList<Game>()

                    val xmlPullParserFactory = XmlPullParserFactory.newInstance()
                    val xmlPullParser = xmlPullParserFactory.newPullParser()

                    xmlPullParser.setInput(StringReader(result))

                    var eventType = xmlPullParser.eventType
                    var currentGame: Game? = null

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_TAG -> {
                                when (xmlPullParser.name) {
                                    "item" -> {
                                        currentGame = Game()
                                        currentGame.bggId =
                                            xmlPullParser.getAttributeValue(null, "objectid")
                                                ?.toLongOrNull()
                                        currentGame.originalTitle =
                                            xmlPullParser.getAttributeValue(null, "name")
                                    }

                                    "name" -> {
                                        val type = xmlPullParser.getAttributeValue(null, "type")
                                        if (type == "primary") {
                                            currentGame?.title = xmlPullParser.nextText()
                                        }
                                    }

                                    "yearpublished" -> {
                                        currentGame?.year =
                                            xmlPullParser.getAttributeValue(null, "value")
                                                ?.toIntOrNull()
                                    }

                                    "thumbnail" -> {
                                        currentGame?.thumbnail = xmlPullParser.nextText()
                                    }
                                }
                            }

                            XmlPullParser.END_TAG -> {
                                if (xmlPullParser.name == "item") {
                                    currentGame?.let { gameList.add(it) }
                                }
                            }
                        }
                        eventType = xmlPullParser.next()
                    }

                    // Insert data into the database
                    val dbHelper = DatabaseHelper(applicationContext)
                    val db = dbHelper.writableDatabase

                    for (gameData in gameList) {
                        val values = ContentValues().apply {
                            put(DatabaseHelper.getColumnTitle(), gameData.title)
                            put(DatabaseHelper.getColumnOriginalTitle(), gameData.originalTitle)
                            put(DatabaseHelper.getColumnYear(), gameData.year)
                            put(DatabaseHelper.getColumnBggId(), gameData.bggId)
                            put(DatabaseHelper.getColumnThumbnail(), gameData.thumbnail)
                        }
                        db.insert(DatabaseHelper.getTableName(), null, values)
                    }
                    db.close()
                }



                val currentDate = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val currentDateStr = currentDate.format(formatter)
                val intent = Intent(this@MainActivity, ProfileActivity::class.java)

                val sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("username", username)
                editor.putInt("totalItems", totalItems)
                editor.putString("currentDateStr", currentDateStr)
                editor.putString("result", result)
                editor.apply()

                startActivity(intent)
            }

        }
    }
}
