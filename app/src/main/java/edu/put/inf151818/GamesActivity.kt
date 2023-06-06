package edu.put.inf151818

import DatabaseHelper
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import java.io.IOException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class GameListAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        // Inflate the layout for each list item
        return LayoutInflater.from(context).inflate(R.layout.list_item_game, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val numberTextView: TextView = view.findViewById(R.id.numberTextView)
        val thumbnailImageView: ImageView = view.findViewById(R.id.thumbnailImageView)
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val yearTextView: TextView = view.findViewById(R.id.yearTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)

        val columnIndexTitle = cursor.getColumnIndexOrThrow(DatabaseHelper.getColumnTitle())
        val columnIndexYear = cursor.getColumnIndexOrThrow(DatabaseHelper.getColumnYear())
        val columnIndexBggId = cursor.getColumnIndexOrThrow(DatabaseHelper.getColumnBggId())

        val title = if (columnIndexTitle != -1) cursor.getString(columnIndexTitle) else ""
        val year = if (columnIndexYear != -1) cursor.getInt(columnIndexYear).toString() else ""
        val bggId = if (columnIndexBggId != -1) cursor.getInt(columnIndexBggId).toString() else ""

        numberTextView.text = (cursor.position + 1).toString()
        titleTextView.text = title
        yearTextView.text = year

        // Fetch thumbnail and description from BoardGameGeek API
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val url = URL("https://www.boardgamegeek.com/xmlapi2/thing?id=$bggId&stats=1")
                val document = withContext(Dispatchers.IO) {
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream())
                }
                val thumbnailUrl = document.getElementsByTagName("thumbnail").item(0).textContent
                val fullDescription = document.getElementsByTagName("description").item(0).textContent

                // Extract first two sentences from the description
                val sentences = fullDescription.split(". ")
                val truncatedDescription = if (sentences.size >= 1) {
                    sentences[0] + ". "
                } else {
                    fullDescription
                }

                descriptionTextView.text = truncatedDescription
                Picasso.get().load(thumbnailUrl).into(thumbnailImageView)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

class GamesActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var cursor: Cursor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)

        dbHelper = DatabaseHelper(this)
        val db: SQLiteDatabase = dbHelper.readableDatabase

        cursor = db.query(
            DatabaseHelper.getTableName(),  // Table name
            null,                          // Columns (null retrieves all columns)
            null,                          // Selection
            null,                          // Selection args
            null,                          // Group by
            null,                          // Having
            null                           // Order by
        )

        val listView: ListView = findViewById(R.id.gameListView)

        val gameListAdapter = GameListAdapter(this, cursor)
        listView.adapter = gameListAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        cursor.close()
        dbHelper.close()
    }
}
