package edu.put.inf151818

import DatabaseHelper
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ListView
import android.widget.TextView

class GameListAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        // Inflate the layout for each list item
        return LayoutInflater.from(context).inflate(R.layout.list_item_game, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val yearTextView: TextView = view.findViewById(R.id.yearTextView)

        val columnIndexTitle = cursor.getColumnIndexOrThrow(DatabaseHelper.getColumnTitle())
        val columnIndexYear = cursor.getColumnIndexOrThrow(DatabaseHelper.getColumnYear())

        val title = if (columnIndexTitle != -1) cursor.getString(columnIndexTitle) else ""
        val year = if (columnIndexYear != -1) cursor.getInt(columnIndexYear).toString() else ""

        titleTextView.text = title
        yearTextView.text = year
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
