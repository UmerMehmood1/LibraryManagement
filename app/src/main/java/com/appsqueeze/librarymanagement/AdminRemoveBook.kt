package com.appsqueeze.librarymanagement

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivityAdminRemoveBookBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings

class AdminRemoveBook : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAdminRemoveBookBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog
    private var book: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRemoveBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        binding.findBook.setOnClickListener(this)
        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
        }
    }

    override fun onClick(v: View) {
        if (v == binding.findBook) {
            handleFindBook()
        }
    }

    private fun handleFindBook() {
        val bookId = binding.editBid1.editText?.text.toString().trim()
        if (bookId.isEmpty()) {
            binding.editBid1.error = "Book Id Required"
            binding.editBid1.isErrorEnabled = true
            return
        }

        progressDialog.setMessage("Please Wait")
        progressDialog.show()

        db.document("Book/$bookId").get()
            .addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        book = document.toObject(Book::class.java)
                        showBookConfirmationDialog()
                    } else {
                        progressDialog.cancel()
                        Toast.makeText(
                            this@AdminRemoveBook,
                            "No such Book found !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    progressDialog.cancel()
                    Toast.makeText(
                        this@AdminRemoveBook,
                        "Failed to fetch book details: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showBookConfirmationDialog() {
        val alert = AlertDialog.Builder(this)
        val temp = """
            Title : ${book?.title}
            Category : ${book?.type}
            No. of Units : ${book?.total}
        """.trimIndent()

        alert.setMessage(temp)
            .setTitle("Please Confirm !")
            .setCancelable(false)
            .setPositiveButton("DELETE") { dialog, which ->
                dialog.dismiss()
                progressDialog.setMessage("Removing ... ")
                progressDialog.show()

                if (book?.available == book?.total) {
                    db.document("Book/${book?.id}")
                        .delete()
                        .addOnSuccessListener {
                            progressDialog.cancel()
                            Toast.makeText(
                                this@AdminRemoveBook,
                                "Book Removed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.cancel()
                            Toast.makeText(
                                this@AdminRemoveBook,
                                "Failed to remove book: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    progressDialog.cancel()
                    Toast.makeText(
                        this@AdminRemoveBook,
                        "This Book is issued to Users!\nReturn before Removing this Book.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("CANCEL") { dialog, which ->
                dialog.dismiss()
            }

        val alertDialog = alert.create()
        alertDialog.show()
    }
}
