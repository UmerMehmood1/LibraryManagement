// AdminIssueBook.kt
package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivityAdminIssueBookBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Calendar

class AdminIssueBook : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAdminIssueBookBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog
    private var userFound = false
    private var bookFound = false
    private var user = User()
    private var book = Book()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminIssueBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        progressDialog = ProgressDialog(this).apply {
            isIndeterminate = true
            setMessage("Please wait...")
        }

        binding.issueButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.issueButton) {
            issueBook()
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    private fun verifyInput(): Boolean {
        val cardNo = binding.editCardNo1.editText?.text.toString().trim()
        val bookId = binding.editBid3.editText?.text.toString().trim()
        var isValid = true

        if (cardNo.isEmpty()) {
            binding.editCardNo1.error = "Card No. Required"
            isValid = false
        } else {
            binding.editCardNo1.isErrorEnabled = false
        }

        if (bookId.isEmpty()) {
            binding.editBid3.error = "Book Id Required"
            isValid = false
        } else {
            binding.editBid3.isErrorEnabled = false
        }

        return isValid
    }

    private fun fetchUser(cardNo: Int) {
        db.collection("User")
            .whereEqualTo("card", cardNo)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    if (task.result?.size() == 1) {
                        userFound = true
                        for (doc in task.result!!) {
                            user = doc.toObject(User::class.java)
                        }
                    } else {
                        userFound = false
                        progressDialog.dismiss()
                        Toast.makeText(this, "No such user!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    userFound = false
                    progressDialog.dismiss()
                    Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchBook(bookId: Int) {
        db.document("Book/$bookId")
            .get()
            .addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
                if (task.isSuccessful) {
                    if (task.result?.exists() == true) {
                        bookFound = true
                        book = task.result!!.toObject(Book::class.java)!!
                    } else {
                        bookFound = false
                        progressDialog.dismiss()
                        Toast.makeText(this, "No such book!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    bookFound = false
                    progressDialog.dismiss()
                    Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun issueBook() {
        Log.d("AdminIssueBook", "issueBook invoked")

        if (!verifyInput()) return

        progressDialog.show()

        val cardNo = binding.editCardNo1.editText?.text.toString().trim().toInt()
        val bookId = binding.editBid3.editText?.text.toString().trim().toInt()

        fetchUser(cardNo)
        fetchBook(bookId)

        if (userFound && bookFound) {
            if (user.book.size >= 5) {
                progressDialog.dismiss()
                Toast.makeText(this, "User already has 5 books issued!", Toast.LENGTH_SHORT).show()
                return
            }
            if (book.available == 0) {
                progressDialog.dismiss()
                Toast.makeText(this, "No units of this book available!", Toast.LENGTH_SHORT).show()
                return
            }
            if (book.units.contains(bookId % 100)) {
                progressDialog.dismiss()
                Toast.makeText(this, "This unit is already issued!", Toast.LENGTH_SHORT).show()
                return
            }

            issueBookToUser(bookId)
        }
    }

    private fun issueBookToUser(bookId: Int) {
        user.book.add(bookId)
        user.fine.add(0)
        user.re.add(1)
        user.date.add(Timestamp(Calendar.getInstance().time))

        db.document("User/${user.email}")
            .set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    book.available -= 1
                    book.units.add(bookId % 100)

                    db.document("Book/${book.id}")
                        .set(book)
                        .addOnCompleteListener { bookTask ->
                            progressDialog.dismiss()
                            if (bookTask.isSuccessful) {
                                Toast.makeText(this, "Book issued successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
