package edu.put.inf151818

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_TOTAL_ITEMS = "extra_total_items"
        const val EXTRA_DATE_STR = "extra_date"
        const val EXTRA_RESULT = "extra_result"
    }

    private lateinit var usernameTextView: TextView
    private lateinit var totalItemsTextView: TextView
    private lateinit var dateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        usernameTextView = findViewById(R.id.usernameTextView)
        totalItemsTextView = findViewById(R.id.totalItemsTextView)
        dateTextView = findViewById(R.id.dateTextView)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        val totalItems = intent.getIntExtra(EXTRA_TOTAL_ITEMS, 0)
        val date = intent.getStringExtra(EXTRA_DATE_STR)
        val result = intent.getStringExtra(EXTRA_RESULT)
        usernameTextView.text = "Username: $username"
        totalItemsTextView.text = "Total items: $totalItems"
        dateTextView.text = "Date of last synchronization: $date"


        val games: Button = findViewById(R.id.games)
        val expansions: Button = findViewById(R.id.expansions)

        games.setOnClickListener {
            // Handle button 1 click
            val intent = Intent(applicationContext, GamesActivity::class.java)
            startActivity(intent)
        }

        expansions.setOnClickListener {
            // Handle button 2 click
            val intent = Intent(applicationContext, ExpansionsActivity::class.java)
            startActivity(intent)
        }
    }
}
