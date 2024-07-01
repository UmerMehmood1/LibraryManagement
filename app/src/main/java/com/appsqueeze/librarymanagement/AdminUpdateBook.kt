package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivityAdminUpdateBookBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class AdminUpdateBook : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAdminUpdateBookBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var p1: ProgressDialog
    private lateinit var book: Book
    private var qtity = 0
    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUpdateBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        p1 = ProgressDialog(this).apply {
            setCancelable(false)
        }

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.list1,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner2.adapter = adapter
        binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                type = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.button2.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v == binding.button2) {
            updateBook()
        }
    }

    private fun verifyTitle(): Boolean {
        val t: String = binding.editTitle2.editText?.text.toString().trim()
        return t.isNotEmpty()
    }

    private fun verifyBid(): Boolean {
        val b: String = binding.editBid2.editText?.text.toString().trim()
        if (b.isEmpty()) {
            binding.editBid2.error = "Book ID Required"
            return true
        }
        return false
    }

    private fun verifyUnits(): Boolean {
        val u: String = binding.editUnits2.editText?.text.toString().trim()
        return u.isNotEmpty()
    }

    private fun verifyCategory(): Boolean {
        return type != "Select Book Category"
    }

    private fun updateBook() {
        if (verifyBid()) return

        if (!(verifyCategory() || verifyTitle() || verifyUnits())) {
            Toast.makeText(this, "Select something to Update !", Toast.LENGTH_SHORT).show()
            return
        }

        p1.setMessage("Updating ...")
        p1.show()

        val bookId = binding.editBid2.editText?.text.toString().trim().toInt()

        db.document("Book/$bookId").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        book = document.toObject(Book::class.java)!!

                        if (verifyCategory()) {
                            book.type = type.toString()
                        }

                        if (verifyUnits()) {
                            val temp1 = book.total
                            book.total = binding.editUnits2.editText?.text.toString().trim().toInt()
                            qtity = book.available - temp1 + book.total
                            book.available = qtity
                        }

                        if (verifyTitle()) {
                            book.title = binding.editTitle2.editText?.text.toString().trim().toUpperCase()
                        }

                        if (qtity >= 0) {
                            db.document("Book/$bookId")
                                .set(book)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        p1.cancel()
                                        Toast.makeText(
                                            this@AdminUpdateBook,
                                            "Updated Successfully !",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        p1.cancel()
                                        Toast.makeText(
                                            this@AdminUpdateBook,
                                            "Try Again !",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            p1.cancel()
                            Toast.makeText(
                                this@AdminUpdateBook,
                                "Can't Reduce No. of Units \ndue to issued units !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        p1.cancel()
                        Toast.makeText(
                            this@AdminUpdateBook,
                            "No Such Book !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    p1.cancel()
                    Toast.makeText(
                        this@AdminUpdateBook,
                        "Try Again !",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}
