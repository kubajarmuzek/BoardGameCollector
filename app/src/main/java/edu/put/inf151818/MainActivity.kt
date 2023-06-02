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

class MainActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var button: Button
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        button = findViewById(R.id.button)
        textView = findViewById(R.id.textView)

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



                val currentDate = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val currentDateStr = currentDate.format(formatter)
                val intent = Intent(applicationContext, ProfileActivity::class.java)
                intent.putExtra(ProfileActivity.EXTRA_USERNAME, username)
                intent.putExtra(ProfileActivity.EXTRA_TOTAL_ITEMS, totalItems)
                intent.putExtra(ProfileActivity.EXTRA_DATE_STR, currentDateStr)
                intent.putExtra(ProfileActivity.EXTRA_RESULT, result)
                startActivity(intent)
            }

        }
    }
}
