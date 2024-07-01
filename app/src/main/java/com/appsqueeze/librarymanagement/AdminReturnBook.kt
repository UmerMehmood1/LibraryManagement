package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivityAdminReturnBookBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdminReturnBook : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAdminReturnBookBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog
    private var U: User = User()
    private var B: Book = Book()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReturnBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        db = Firebase.firestore

        binding.returnButton.setOnClickListener(this)
        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
        }
    }

    override fun onClick(v: View) {
        if (v == binding.returnButton) {
            returnBook()
        }
    }

    private fun verifyCard(): Boolean {
        val t: String = binding.editCardNo2.editText?.text.toString().trim()
        if (t.isEmpty()) {
            binding.editCardNo2.error = "Card No. Required"
            return true
        } else {
            binding.editCardNo2.error = null
            return false
        }
    }

    private fun verifyBid(): Boolean {
        val t: String = binding.editBid4.editText?.text.toString().trim()
        if (t.isEmpty()) {
            binding.editBid4.error = "Book Id Required"
            return true
        } else {
            binding.editBid4.error = null
            return false
        }
    }

    private val user: Boolean
        get() {
            var res1 = false
            val cardNo = binding.editCardNo2.editText?.text.toString().trim().toInt()
            db.collection("User")
                .whereEqualTo("card", cardNo)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result?.size() == 1) {
                            for (doc in task.result!!) {
                                U = doc.toObject(User::class.java)
                            }
                            res1 = true
                        } else {
                            res1 = false
                            progressDialog.cancel()
                            Toast.makeText(
                                this@AdminReturnBook,
                                "No Such User !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        res1 = false
                        progressDialog.cancel()
                        Toast.makeText(
                            this@AdminReturnBook,
                            "Try Again !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            return res1
        }

    private val book: Boolean
        get() {
            val bookId = binding.editBid4.editText?.text.toString().trim().toInt() / 100
            var res2 = false
            db.document("Book/$bookId")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result?.exists() == true) {
                            B = task.result!!.toObject(Book::class.java)!!
                            res2 = true
                        } else {
                            res2 = false
                            progressDialog.cancel()
                            Toast.makeText(
                                this@AdminReturnBook,
                                "No Such Book !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        res2 = false
                        progressDialog.cancel()
                        Toast.makeText(
                            this@AdminReturnBook,
                            "Try Again !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            return res2
        }

    private fun returnBook() {
        if (verifyBid() || verifyCard()) return

        progressDialog.setMessage("Please Wait !")
        progressDialog.show()

        if (user && book) {
            val bookId = binding.editBid4.editText?.text.toString().trim().toInt()
            if (!U.book.contains(bookId)) {
                progressDialog.cancel()
                Toast.makeText(
                    this@AdminReturnBook,
                    "Given Book is not issued to the User !",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val index = U.book.indexOf(bookId)
            U.left_fine += U.fine[index]
            U.book = U.book.filterIndexed { idx, _ -> idx != index }.toMutableList()
            U.fine = U.fine.filterIndexed { idx, _ -> idx != index }.toMutableList()
            U.re = U.re.filterIndexed { idx, _ -> idx != index }.toMutableList()
            U.date = U.date.filterIndexed { idx, _ -> idx != index }.toMutableList()

            db.document("User/${U.email}")
                .set(U)
                .addOnCompleteListener { userTask ->
                    if (userTask.isSuccessful) {
                        B.available++
                        val unitsIndex = B.units.indexOf(bookId % 100)
                        B.units = B.units.filterIndexed { idx, _ -> idx != unitsIndex }.toMutableList()

                        db.document("Book/${B.id}")
                            .set(B)
                            .addOnCompleteListener { bookTask ->
                                if (bookTask.isSuccessful) {
                                    progressDialog.cancel()
                                    Toast.makeText(
                                        this@AdminReturnBook,
                                        "Book Returned Successfully !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    progressDialog.cancel()
                                    Toast.makeText(
                                        this@AdminReturnBook,
                                        "Failed to update Book details !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        progressDialog.cancel()
                        Toast.makeText(
                            this@AdminReturnBook,
                            "Failed to update User details !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
