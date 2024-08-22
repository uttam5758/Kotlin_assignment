package com.example.kotlinapp
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

class NoteDetailsActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveNoteBtn: ImageButton
    private lateinit var pageTitleTextView: TextView
    private lateinit var deleteNoteTextViewBtn: TextView

    private var title: String? = null
    private var content: String? = null
    private var docId: String? = null
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)

        // Initialize views
        titleEditText = findViewById(R.id.notes_title_text)
        contentEditText = findViewById(R.id.notes_content_text)
        saveNoteBtn = findViewById(R.id.save_note_btn)
        pageTitleTextView = findViewById(R.id.page_title)
        deleteNoteTextViewBtn = findViewById(R.id.delete_note_text_view_btn)

        // Receive data from intent
        intent?.let {
            title = it.getStringExtra("title")
            content = it.getStringExtra("content")
            docId = it.getStringExtra("docId")
        }

        if (!docId.isNullOrEmpty()) {
            isEditMode = true
        }

        // Populate fields if in edit mode
        titleEditText.setText(title)
        contentEditText.setText(content)

        if (isEditMode) {
            pageTitleTextView.text = "Edit your note"
            deleteNoteTextViewBtn.visibility = View.VISIBLE
        }

        // Set listeners
        saveNoteBtn.setOnClickListener { saveNote() }
        deleteNoteTextViewBtn.setOnClickListener { deleteNoteFromFirebase() }
    }

    private fun saveNote() {
        val noteTitle = titleEditText.text.toString()
        val noteContent = contentEditText.text.toString()

        if (noteTitle.isEmpty()) {
            titleEditText.error = "Title is required"
            return
        }

        val note = Note(
            title = noteTitle,
            content = noteContent,
            timestamp = Timestamp.now()
        )

        saveNoteToFirebase(note)
    }

    private fun saveNoteToFirebase(note: Note) {
        val documentReference: DocumentReference = if (isEditMode) {
            // Update existing note
            Utility.collectionReferenceForNotes.document(docId!!)
        } else {
            // Create a new note
            Utility.collectionReferenceForNotes.document()
        }

        documentReference.set(note).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Utility.showToast(this, "Note saved successfully")
                finish()
            } else {
                Utility.showToast(this, "Failed while saving note")
            }
        }
    }

    private fun deleteNoteFromFirebase() {
        val documentReference = Utility.collectionReferenceForNotes.document(docId!!)

        documentReference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Utility.showToast(this, "Note deleted successfully")
                finish()
            } else {
                Utility.showToast(this, "Failed while deleting note")
            }
        }
    }
}