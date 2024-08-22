package com.example.kotlinapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.example.kotlinapp.NoteAdapter.NoteViewHolder

class NoteAdapter(
    options: FirestoreRecyclerOptions<Note>,
    private val context: Context
) : FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder>(options) {

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, note: Note) {
        holder.bind(note)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, NoteDetailsActivity::class.java).apply {
                putExtra("title", note.title)
                putExtra("content", note.content)
                putExtra("docId", snapshots.getSnapshot(position).id)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_note_item, parent, false)
        return NoteViewHolder(view)
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.note_title_text_view)
        private val contentTextView: TextView = itemView.findViewById(R.id.note_content_text_view)
        private val timestampTextView: TextView = itemView.findViewById(R.id.note_timestamp_text_view)

        fun bind(note: Note) {
            titleTextView.text = note.title
            contentTextView.text = note.content
            // Since timestamp is non-nullable, we can directly use it
            timestampTextView.text = Utility.timestampToString(note.timestamp)
        }
    }
}