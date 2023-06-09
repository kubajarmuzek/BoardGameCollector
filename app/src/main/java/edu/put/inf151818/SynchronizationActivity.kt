package edu.put.inf151818

import DatabaseHelper
import ExpansionDatabaseHelper
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SynchronizationActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synchronization)
        val profileButton: Button = findViewById(R.id.profile)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        val syncButton: Button = findViewById(R.id.synchro)
        val deleteData: Button = findViewById(R.id.deleteData)

        syncButton.setOnClickListener {
            val dbHelper = DatabaseHelper(applicationContext)
            dbHelper.resetDatabase()
            val EdbHelper = ExpansionDatabaseHelper(applicationContext)
            EdbHelper.resetDatabase()
            sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val username = sharedPreferences.getString("username", "")
            FetchDataTask().execute(username)
            FetchExpansionDataTask().execute(username)
        }

        deleteData.setOnClickListener {
            val dbHelper = DatabaseHelper(applicationContext)
            dbHelper.resetDatabase()
            val EdbHelper = ExpansionDatabaseHelper(applicationContext)
            EdbHelper.resetDatabase()
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

    }

    private inner class FetchExpansionDataTask : AsyncTask<String, Void, String>() {
        private var username: String? = null
        private var totalItems: String? = null
        override fun doInBackground(vararg params: String?): String {
            username = params.getOrNull(0)

            val url = URL("https://www.boardgamegeek.com/xmlapi2/collection?username=$username&subtype=boardgameexpansion&stats=1")
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

                if (username != null) {
                    // ... Parsing XML and obtaining game information ...

                    val gameList = ArrayList<Game>()

                    val xmlPullParserFactory = XmlPullParserFactory.newInstance()
                    val xmlPullParser = xmlPullParserFactory.newPullParser()

                    xmlPullParser.setInput(StringReader(result))

                    var eventType = xmlPullParser.eventType
                    var currentGame: Game? = null
                    var currentTag: String? = null // Track the current XML tag
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {

                            XmlPullParser.START_TAG -> {
                                currentTag = xmlPullParser.name
                                when (xmlPullParser.name) {
                                    "item" -> {
                                        currentGame = Game()
                                        currentGame.bggId = xmlPullParser.getAttributeValue(null, "objectid")?.toLongOrNull()
                                        currentGame.originalTitle = xmlPullParser.getAttributeValue(null, "name")
                                    }

                                    "name" -> {
                                        if (currentGame != null) {
                                            val sortIndex = xmlPullParser.getAttributeValue(null, "sortindex")
                                            if (sortIndex == "1") {
                                                currentGame.title = xmlPullParser.nextText()
                                            }
                                            if (sortIndex == "5") {
                                                currentGame.title = xmlPullParser.nextText()
                                            }
                                        }
                                    }

                                    "yearpublished" -> {
                                        if (currentGame != null) {
                                            //currentGame.year = xmlPullParser.getAttributeValue(null, "value")?.toIntOrNull()
                                        }
                                    }

                                    "thumbnail" -> {
                                        currentGame?.thumbnail = xmlPullParser.nextText()
                                    }
                                }
                            }

                            XmlPullParser.TEXT -> {
                                val text = xmlPullParser.text?.trim()
                                if (currentGame != null && !text.isNullOrEmpty()) {
                                    when (currentTag) {
                                        "name" -> {
                                            val sortIndex = xmlPullParser.getAttributeValue(null, "sortindex")
                                            if (sortIndex == "1") {
                                                //currentGame.title = text
                                            }
                                        }
                                        "yearpublished" -> {
                                            currentGame.year = text.toIntOrNull()
                                        }
                                        // Add other cases if needed for additional tags
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
                    val dbHelper = ExpansionDatabaseHelper(applicationContext)
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
                }
                totalItems = totalItems?.minus(0) ?: 0

                val sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("totalExpansioins", totalItems)
                editor.putString("result", result)
                editor.apply()
            }

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
                    var currentTag: String? = null // Track the current XML tag
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {

                            XmlPullParser.START_TAG -> {
                                currentTag = xmlPullParser.name
                                when (xmlPullParser.name) {
                                    "item" -> {
                                        currentGame = Game()
                                        currentGame.bggId = xmlPullParser.getAttributeValue(null, "objectid")?.toLongOrNull()
                                        currentGame.originalTitle = xmlPullParser.getAttributeValue(null, "name")
                                    }

                                    "name" -> {
                                        if (currentGame != null) {
                                            val sortIndex = xmlPullParser.getAttributeValue(null, "sortindex")
                                            if (sortIndex == "1") {
                                                currentGame.title = xmlPullParser.nextText()
                                            }
                                            if (sortIndex == "5") {
                                                currentGame.title = xmlPullParser.nextText()
                                            }
                                        }
                                    }

                                    "yearpublished" -> {
                                        if (currentGame != null) {
                                            //currentGame.year = xmlPullParser.getAttributeValue(null, "value")?.toIntOrNull()
                                        }
                                    }

                                    "thumbnail" -> {
                                        currentGame?.thumbnail = xmlPullParser.nextText()
                                    }
                                }
                            }

                            XmlPullParser.TEXT -> {
                                val text = xmlPullParser.text?.trim()
                                if (currentGame != null && !text.isNullOrEmpty()) {
                                    when (currentTag) {
                                        "name" -> {
                                            val sortIndex = xmlPullParser.getAttributeValue(null, "sortindex")
                                            if (sortIndex == "1") {
                                                //currentGame.title = text
                                            }
                                        }
                                        "yearpublished" -> {
                                            currentGame.year = text.toIntOrNull()
                                        }
                                        // Add other cases if needed for additional tags
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
                }



                val currentDate = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val currentDateStr = currentDate.format(formatter)
                val intent = Intent(this@SynchronizationActivity, ProfileActivity::class.java)

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