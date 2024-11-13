package com.example.autohelper_pms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class NotesFragment : Fragment() {
    private lateinit var noteList: MutableList<Pair<String, String>>
    private lateinit var adapter: NotesAdapter
    private lateinit var username: String

    companion object {
        private const val ARG_USERNAME = "username"

        fun newInstance(username: String): NotesFragment {
            val fragment = NotesFragment()
            val args = Bundle()
            args.putString(ARG_USERNAME, username)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        username = arguments?.getString(ARG_USERNAME) ?: "default_user"
        noteList = loadNoteList(requireContext(), username)

        val noteInput: EditText = view.findViewById(R.id.note_input)
        val noteDateInput: EditText = view.findViewById(R.id.note_date_input)
        val addNoteButton: Button = view.findViewById(R.id.add_note_button)
        val recyclerView: RecyclerView = view.findViewById(R.id.note_recycler_view)

        adapter = NotesAdapter(noteList) { position ->
            // No action on item click
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.onItemMove(fromPosition, toPosition)
                saveNoteList(requireContext(), username, noteList)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                noteList.removeAt(position)
                adapter.notifyItemRemoved(position)
                saveNoteList(requireContext(), username, noteList)
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        addNoteButton.setOnClickListener {
            val noteName = noteInput.text.toString().trim()
            val noteDateStr = noteDateInput.text.toString().trim()
            if (noteName.isNotEmpty() && noteDateStr.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                try {
                    sdf.parse(noteDateStr)
                    val newNote = noteName to noteDateStr
                    noteList.add(newNote)
                    adapter.notifyItemInserted(noteList.size - 1)
                    noteInput.text.clear()
                    noteDateInput.text.clear()
                    saveNoteList(requireContext(), username, noteList)
                } catch (e: ParseException) {
                    Toast.makeText(requireContext(), "Неправильный формат даты. Используйте формат dd.MM.yyyy", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    private fun saveNoteList(context: Context, username: String, noteList: List<Pair<String, String>>) {
        val file = File(context.filesDir, "note_data_$username.txt")
        val data = noteList.joinToString("\n") { "${it.first}|${it.second}" }
        file.writeText(data)
    }

    private fun loadNoteList(context: Context, username: String): MutableList<Pair<String, String>> {
        val file = File(context.filesDir, "note_data_$username.txt")
        return if (file.exists()) {
            file.readLines().map {
                val parts = it.split("|")
                parts[0] to parts[1]
            }.toMutableList()
        } else {
            mutableListOf()
        }
    }
}