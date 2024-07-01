package com.appsqueeze.librarymanagement

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivityAdminCollectFineBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class AdminCollectFine : AppCompatActivity() {

    private lateinit var binding: ActivityAdminCollectFineBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCollectFineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        progressDialog = createProgressDialog()
        firestore = FirebaseFirestore.getInstance()

        binding.collect.setOnClickListener {
            if (verifyUser()) return@setOnClickListener
            progressDialog.show()

            val cardNumber = binding.editUser.editText?.text.toString().trim().toInt()
            fetchUser(cardNumber)
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    private fun createProgressDialog(): ProgressDialog {
        return ProgressDialog(this).apply {
            setMessage("Please Wait!")
            setCancelable(false)
        }
    }

    private fun verifyUser(): Boolean {
        val cardNumber = binding.editUser.editText?.text.toString().trim()
        return if (cardNumber.isEmpty()) {
            binding.editUser.error = "Card No. Required"
            true
        } else {
            binding.editUser.isErrorEnabled = false
            false
        }
    }

    private fun fetchUser(cardNumber: Int) {
        firestore.collection("User").whereEqualTo("card", cardNumber).get()
            .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot?> {
                override fun onComplete(task: Task<QuerySnapshot?>) {
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        if (!(task.result?.isEmpty)!!) {
                            for (document in task.result!!) {
                                user = document.toObject(User::class.java)
                            }
                            collectFine()
                        } else {
                            showToast("No Such User!")
                        }
                    } else {
                        showToast("Try Again!")
                    }
                }
            })
    }

    private fun collectFine() {
        user?.let { user ->
            var totalFine = user.left_fine
            totalFine += user.fine.sum()

            if (totalFine == 0) {
                showToast("This User has no Fine!")
                return
            }

            AlertDialog.Builder(this)
                .setTitle("Collect Fine!")
                .setMessage("Collect Rs.$totalFine from ${user.name}")
                .setCancelable(false)
                .setPositiveButton("Collect") { dialog, _ ->
                    dialog.dismiss()
                    processFineCollection(user)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private fun processFineCollection(user: User) {
        progressDialog.show()
        user.fine = MutableList(user.fine.size) { 0 }
        user.left_fine = 0

        firestore.document("User/${user.email}").set(user)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    showToast("Fine Collected!")
                } else {
                    showToast("Try Again!")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
