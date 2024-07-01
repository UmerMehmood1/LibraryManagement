package com.appsqueeze.librarymanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat

class MyBookAdapter(private val myBooks: List<MyBook>) : RecyclerView.Adapter<MyBookAdapter.MyBookViewHolder>() {
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yy")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.see_my_book_view, parent, false)
        return MyBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyBookViewHolder, position: Int) {
        val myBook = myBooks[position]
        holder.bind(myBook)
    }

    override fun getItemCount(): Int {
        return myBooks.size
    }

    inner class MyBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookName: TextView = itemView.findViewById(R.id.bookName1)
        private val bookId: TextView = itemView.findViewById(R.id.bookId1)
        private val bookType: TextView = itemView.findViewById(R.id.bookType1)
        private val bookIssueDate: TextView = itemView.findViewById(R.id.bookIdate)
        private val bookDueDate: TextView = itemView.findViewById(R.id.bookDdate)

        fun bind(myBook: MyBook) {
            bookId.text = "ID : ${myBook.bid}"
            bookName.text = "Title : ${myBook.title}"
            bookType.text = "Type : ${myBook.type}"
            bookIssueDate.text = "Issue Date : ${simpleDateFormat.format(myBook.idate)}"
            bookDueDate.text = "Due Date : ${simpleDateFormat.format(myBook.ddate)}"
        }
    }
}
