package com.example.sqlite
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: NotesDatabaseHelper
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = NotesDatabaseHelper(this)

        titleEditText = findViewById(R.id.editTextTitle)
        contentEditText = findViewById(R.id.editTextContent)
        saveButton = findViewById(R.id.buttonSave)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        notesAdapter = NotesAdapter(getAllNotes())
        recyclerView.adapter = notesAdapter

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                insertNote(title, content)
                titleEditText.text.clear()
                contentEditText.text.clear()
                notesAdapter.updateNotes(getAllNotes())
            } else {
                showToast("Please enter a title and content.")
            }
        }
    }

    private fun insertNote(title: String, content: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put(NoteEntry.COLUMN_TITLE, title)
        values.put(NoteEntry.COLUMN_CONTENT, content)
        db.insert(NoteEntry.TABLE_NAME, null, values)
        db.close()
    }

    private fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${NoteEntry.TABLE_NAME}", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(NoteEntry._ID))
                val title = cursor.getString(cursor.getColumnIndex(NoteEntry.COLUMN_TITLE))
                val content = cursor.getString(cursor.getColumnIndex(NoteEntry.COLUMN_CONTENT))
                notes.add(Note(id, title, content))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return notes
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        // Define the database schema
        private const val DATABASE_NAME = "notes.db"
        private const val DATABASE_VERSION = 1

        object NoteEntry {
            const val TABLE_NAME = "notes"
            const val _ID = "_id"
            const val COLUMN_TITLE = "title"
            const val COLUMN_CONTENT = "content"
        }
    }
}

data class Note(val id: Int, val title: String, val content: String)

class NotesDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE ${MainActivity.NoteEntry.TABLE_NAME} (
                ${MainActivity.NoteEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${MainActivity.NoteEntry.COLUMN_TITLE} TEXT,
                ${MainActivity.NoteEntry.COLUMN_CONTENT} TEXT
            );
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${MainActivity.NoteEntry.TABLE_NAME}")
        onCreate(db)
    }
}

class NotesAdapter(private var notes: List<Note>) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content
    }

    override fun getItemCount(): Int = notes.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView  = itemView.findViewById(R.id.textTitle)
        val contentTextView: TextView = itemView.findViewById(R.id.textContent)
    }

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}
