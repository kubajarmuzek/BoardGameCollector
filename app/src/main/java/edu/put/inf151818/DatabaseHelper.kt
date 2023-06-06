import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "board_games.db"
        private const val DATABASE_VERSION = 1

        // Define table and column names
        private const val TABLE_NAME = "games"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_ORIGINAL_TITLE = "original_title"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_BGG_ID = "bgg_id"
        private const val COLUMN_THUMBNAIL = "thumbnail"

        fun getColumnTitle(): String {
            return COLUMN_TITLE
        }

        fun getColumnOriginalTitle(): String {
            return COLUMN_ORIGINAL_TITLE
        }

        fun getTableName(): String {
            return TABLE_NAME
        }

        fun getColumnYear(): String {
            return COLUMN_YEAR
        }

        fun getColumnBggId(): String {
            return COLUMN_BGG_ID
        }

        fun getColumnThumbnail(): String {
            return COLUMN_THUMBNAIL
        }
    }
    fun resetDatabase() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '$TABLE_NAME'")
        db.close()
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create the table
        val createTableQuery = "CREATE TABLE $TABLE_NAME " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TITLE TEXT, " +
                "$COLUMN_ORIGINAL_TITLE TEXT, " +
                "$COLUMN_YEAR INTEGER, " +
                "$COLUMN_BGG_ID INTEGER, " +
                "$COLUMN_THUMBNAIL TEXT)"

        db.execSQL(createTableQuery)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades, if needed
        // This method is called when DATABASE_VERSION is incremented
    }
}


