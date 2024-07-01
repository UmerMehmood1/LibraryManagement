package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.appsqueeze.librarymanagement.databinding.ActivityUserSeeMyBooksBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

class UserSeeMyBooks : AppCompatActivity() {

    private lateinit var binding: ActivityUserSeeMyBooksBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var ifNoBook1: TextView
    private lateinit var myBooks: MutableList<MyBook>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSeeMyBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait ...")
        progressDialog.setCancelable(false)

        ifNoBook1 = binding.ifNoBook1
        myBooks = ArrayList()

        setupRecyclerView()
        fetchUserBooks()
    }

    private fun setupRecyclerView() {
        binding.recycle1.layoutManager = LinearLayoutManager(this)
        binding.recycle1.setHasFixedSize(true)
        binding.recycle1.adapter = MyBookAdapter(myBooks)
    }

    private fun fetchUserBooks() {
        progressDialog.show()

        db.document("User/${firebaseAuth.currentUser?.email}")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.toObject(User::class.java)

                    if (user != null && !user.book.isEmpty()) {
                        loadBooks(user)
                    } else {
                        progressDialog.cancel()
                        ifNoBook1.text = "YOU HAVE NO ISSUED BOOKS !"
                        ifNoBook1.textSize = 18f
                    }
                } else {
                    progressDialog.cancel()
                    // Handle error
                }
            }
    }

    private fun loadBooks(user: User) {
        var count = 0

        for (bookId in user.book) {
            db.document("Book/${bookId / 100}")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val book = task.result?.toObject(Book::class.java)

                        if (book != null) {
                            val currentDate = Calendar.getInstance().time
                            val dueDate = Calendar.getInstance()
                            dueDate.time = user.date[count].toDate()
                            dueDate.add(Calendar.DAY_OF_MONTH, 14)

                            myBooks.add(MyBook(bookId, book.title, book.type, user.date[count].toDate(), dueDate.time))
                            binding.recycle1.adapter?.notifyItemInserted(myBooks.size - 1)
                        }

                        count++
                        if (count == user.book.size) {
                            progressDialog.cancel()
                        }
                    } else {
                        progressDialog.cancel()
                        // Handle error
                    }
                }
        }
    }
}
