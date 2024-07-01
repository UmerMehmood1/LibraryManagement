package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsqueeze.librarymanagement.databinding.ActivitySearchBookBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.FirestoreRecyclerOptions.Builder

class SearchBook : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBookBinding
    private var db: FirebaseFirestore? = null
    private var adapter: FirestoreRecyclerAdapter<Book, BookViewHolder>? = null
    private var mode = 0
    private var key: String? = ""
    private var progressDialog: ProgressDialog? = null
    private var ifNoBook: TextView? = null
    private var query: Query? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()

        val intent: Intent = intent

        when (intent.getIntExtra("id", 0)) {
            1 -> {
                mode = 0
                query = db?.collection("Book")?.whereEqualTo("id", intent.getIntExtra("bid", 0))
                    ?.whereGreaterThan("available", 0)
            }
            2 -> {
                mode = 0
                query = db?.collection("Book")?.whereEqualTo("id", intent.getIntExtra("bid", 0))
            }
            3 -> {
                mode = 1
                key = intent.getStringExtra("btitle")
                query = db?.collection("Book")?.whereEqualTo("type", intent.getStringExtra("btype"))
                    ?.whereGreaterThan("available", 0)
            }
            4 -> {
                mode = 1
                key = intent.getStringExtra("btitle")
                query = db?.collection("Book")?.whereEqualTo("type", intent.getStringExtra("btype"))
            }
            5 -> {
                mode = 1
                key = intent.getStringExtra("btitle")
                query = db?.collection("Book")?.whereGreaterThan("available", 0)
            }
            6 -> {
                mode = 1
                key = intent.getStringExtra("btitle")
                query = db?.collection("Book")
            }
            7 -> {
                mode = 0
                query = db?.collection("Book")?.whereEqualTo("type", intent.getStringExtra("btype"))
                    ?.whereGreaterThan("available", 0)
            }
            8 -> {
                mode = 0
                query = db?.collection("Book")?.whereEqualTo("type", intent.getStringExtra("btype"))
            }
        }

        val options: FirestoreRecyclerOptions<Book> =
            Builder<Book>().setQuery(query!!, Book::class.java).build()

        adapter = object : FirestoreRecyclerAdapter<Book, BookViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.book_view, parent, false)
                return BookViewHolder(view)
            }

            override fun onBindViewHolder(holder: BookViewHolder, position: Int, model: Book) {
                holder.bind(model)
            }
        }

        binding.recycle.layoutManager = LinearLayoutManager(this)
        binding.recycle.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    private fun setupViews() {
        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Please Wait !")
        ifNoBook = binding.ifNoBook
    }

    private inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookName: TextView = itemView.findViewById(R.id.bookName)
        private val bookId: TextView = itemView.findViewById(R.id.bookId)
        private val bookType: TextView = itemView.findViewById(R.id.bookType)
        private val bookAvailable: TextView = itemView.findViewById(R.id.bookAvailable)
        private val bookTotal: TextView = itemView.findViewById(R.id.bookTotal)

        fun bind(book: Book) {
            bookName.text = "Title : ${book.title}"
            bookId.text = "ID : ${book.id}"
            bookType.text = "Category : ${book.type}"
            bookAvailable.text = "Available : ${book.available}"
            bookTotal.text = "Total : ${book.total}"
        }
    }
}
