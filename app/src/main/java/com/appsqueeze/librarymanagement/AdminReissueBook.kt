// AdminReissueBook.kt
package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivityAdminReissueBookBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Calendar

class AdminReissueBook : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAdminReissueBookBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog
    private var userFound = false
    private var user = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReissueBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        progressDialog = ProgressDialog(this).apply {
            isIndeterminate = true
            setMessage("Please wait...")
        }

        binding.reissueButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.reissueButton) {
            reissueBook()
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    private fun verifyInput(): Boolean {
        val cardNo = binding.editCardNo5.editText?.text.toString().trim()
        val bookId = binding.editBid5.editText?.text.toString().trim()
        var isValid = true

        if (cardNo.isEmpty()) {
            binding.editCardNo5.error = "Card No. Required"
            isValid = false
        } else {
            binding.editCardNo5.isErrorEnabled = false
        }

        if (bookId.isEmpty()) {
            binding.editBid5.error = "Book Id Required"
            isValid = false
        } else {
            binding.editBid5.isErrorEnabled = false
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

    private fun reissueBook() {
        Log.d("AdminReissueBook", "reissueBook invoked")

        if (!verifyInput()) return

        progressDialog.show()

        val cardNo = binding.editCardNo5.editText?.text.toString().trim().toInt()
        val bookId = binding.editBid5.editText?.text.toString().trim().toInt()

        fetchUser(cardNo)

        if (userFound) {
            if (!user.book.contains(bookId)) {
                progressDialog.dismiss()
                Toast.makeText(this, "This book is not issued to this user!", Toast.LENGTH_SHORT).show()
                return
            }

            val bookIndex = user.book.indexOf(bookId)
            user.left_fine += user.fine[bookIndex]
            user.fine = user.fine.toMutableList().apply { set(bookIndex, 0) }
            user.re = user.re.toMutableList().apply { set(bookIndex, 1) }
            user.date = user.date.toMutableList().apply {
                set(bookIndex, Timestamp(Calendar.getInstance().time))
            }

            db.document("User/${user.email}")
                .set(user)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Re-issued successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please try again!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
