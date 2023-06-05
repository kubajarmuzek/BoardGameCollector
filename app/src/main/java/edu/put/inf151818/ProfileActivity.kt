package edu.put.inf151818

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usernameTextView: TextView
    private lateinit var totalItemsTextView: TextView
    private lateinit var dateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        usernameTextView = findViewById(R.id.usernameTextView)
        totalItemsTextView = findViewById(R.id.totalItemsTextView)
        dateTextView = findViewById(R.id.dateTextView)

        val username = sharedPreferences.getString("username", "")
        val totalItems = sharedPreferences.getInt("totalItems", 0)
        val date = sharedPreferences.getString("currentDateStr", "")
        val result = sharedPreferences.getString("result", "")
        usernameTextView.text = "Username: $username"
        totalItemsTextView.text = "Total items: $totalItems"
        dateTextView.text = "Date of last synchronization: $date"

        val games: Button = findViewById(R.id.games)
        val expansions: Button = findViewById(R.id.expansions)



        games.setOnClickListener {
            val intent = Intent(applicationContext, GamesActivity::class.java)
            startActivity(intent)
        }

        expansions.setOnClickListener {
            val intent = Intent(applicationContext, ExpansionsActivity::class.java)
            startActivity(intent)
        }
    }
}

