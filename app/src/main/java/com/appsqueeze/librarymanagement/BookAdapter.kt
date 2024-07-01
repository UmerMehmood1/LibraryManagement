package com.appsqueeze.librarymanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class BookAdapter(
    options: FirestoreRecyclerOptions<Book?>,
    private val key: String,
    private val mode: Int
) : FirestoreRecyclerAdapter<Book?, BookAdapter.BookItemViewHolder>(options) {

    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int, model: Book) {
        holder.bind(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.book_view, parent, false)
        return BookItemViewHolder(view)
    }

    inner class BookItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookName: TextView = itemView.findViewById(R.id.bookName)
        private val bookId: TextView = itemView.findViewById(R.id.bookId)
        private val bookType: TextView = itemView.findViewById(R.id.bookType)
        private val bookAvailable: TextView = itemView.findViewById(R.id.bookAvailable)
        private val bookTotal: TextView = itemView.findViewById(R.id.bookTotal)

        fun bind(book: Book) {
            bookId.text = "ID : ${book.id}"
            bookType.text = "Category : ${book.type}"
            bookAvailable.text = "Available : ${book.available}"
            bookName.text = "Title : ${book.title}"
            bookTotal.text = "Total : ${book.total}"

            if (!book.title?.contains(key)!! && mode == 1) {
                itemView.visibility = View.GONE
                val layoutParams = itemView.layoutParams as RecyclerView.LayoutParams
                layoutParams.height = 0
                layoutParams.width = 0
                itemView.layoutParams = layoutParams
            } else {
                itemView.visibility = View.VISIBLE
                val layoutParams = itemView.layoutParams as RecyclerView.LayoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                itemView.layoutParams = layoutParams
            }
        }
    }
}
