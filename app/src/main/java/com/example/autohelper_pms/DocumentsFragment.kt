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

class DocumentsFragment : Fragment() {
    private lateinit var docList: MutableList<Pair<String, String>>
    private lateinit var adapter: DocumentsAdapter
    private lateinit var username: String

    companion object {
        private const val ARG_USERNAME = "username"

        fun newInstance(username: String): DocumentsFragment {
            val fragment = DocumentsFragment()
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
        val view = inflater.inflate(R.layout.fragment_documents, container, false)

        username = arguments?.getString(ARG_USERNAME) ?: "default_user"
        docList = loadDocumentList(requireContext(), username)

        val documentInput: EditText = view.findViewById(R.id.document_input)
        val documentDateInput: EditText = view.findViewById(R.id.document_data_input)
        val addDocumentButton: Button = view.findViewById(R.id.add_document_button)
        val recyclerView: RecyclerView = view.findViewById(R.id.documents_recycler_view)

        adapter = DocumentsAdapter(docList) { position ->
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
                saveDocumentList(requireContext(), username, docList)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                docList.removeAt(position)
                adapter.notifyItemRemoved(position)
                saveDocumentList(requireContext(), username, docList)
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        addDocumentButton.setOnClickListener {
            val documentName = documentInput.text.toString().trim()
            val documentDateStr = documentDateInput.text.toString().trim()
            if (documentName.isNotEmpty() && documentDateStr.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                try {
                    sdf.parse(documentDateStr)
                    val newDocument = documentName to documentDateStr
                    docList.add(newDocument)
                    adapter.notifyItemInserted(docList.size - 1)
                    documentInput.text.clear()
                    documentDateInput.text.clear()
                    saveDocumentList(requireContext(), username, docList)
                } catch (e: ParseException) {
                    Toast.makeText(requireContext(), "Неправильный формат даты. Используйте формат dd.MM.yyyy", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    private fun saveDocumentList(context: Context, carName: String, documentList: List<Pair<String, String>>) {
        val file = File(context.filesDir, "document_data_$carName.txt")
        val data = documentList.joinToString("\n") { "${it.first}|${it.second}" }
        file.writeText(data)
    }

    private fun loadDocumentList(context: Context, carName: String): MutableList<Pair<String, String>> {
        val file = File(context.filesDir, "document_data_$carName.txt")
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