package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.appsqueeze.librarymanagement.databinding.ActivitySigninBinding
import com.google.firebase.FirebaseApp

class SignInActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySigninBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (firebaseAuth.currentUser != null) {
            progressDialog.setMessage("Please Wait... Signing You in !")
            progressDialog.show()
            val cur = firebaseAuth.currentUser?.email?.trim() ?: ""
            db.document("User/$cur").get()
                .addOnSuccessListener { documentSnapshot ->
                    val obj = documentSnapshot.toObject(User::class.java)
                    if (obj?.type == 0) {
                        progressDialog.cancel()
                        startActivity(Intent(this@SignInActivity, UserHome::class.java))
                        finish()
                    } else {
                        progressDialog.cancel()
                        startActivity(Intent(this@SignInActivity, AdminHome::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.cancel()
                    Toast.makeText(this@SignInActivity, "Please Sign in Again", Toast.LENGTH_SHORT)
                        .show()
                }
        }

        binding.buttonSignIn.setOnClickListener(this)
        binding.toSignUp.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonSignIn -> signinUser()
            R.id.toSignUp -> navigateToSignUp()
        }
    }

    private fun verifyEmailId(): Boolean {
        val emailId = binding.editID.editText?.text.toString().trim()
        return if (emailId.isEmpty()) {
            binding.editID.error = "Email ID Required"
            true
        } else {
            binding.editID.error = null
            false
        }
    }

    private fun verifyPass(): Boolean {
        val pass = binding.editPass.editText?.text.toString().trim()
        return if (pass.isEmpty()) {
            binding.editPass.error = "Password Required"
            true
        } else {
            binding.editPass.error = null
            false
        }
    }

    private fun signinUser() {
        if (verifyEmailId() || verifyPass()) {
            return
        }

        val id = "${binding.editID.editText?.text.toString().trim()}@iiitnr.edu.in"
        val pass = binding.editPass.editText?.text.toString().trim()

        progressDialog.setMessage("Signing In ... ")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(id, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.email ?: ""
                    db.collection("User").document(userId).get()
                        .addOnSuccessListener { documentSnapshot ->
                            val user = documentSnapshot.toObject(User::class.java)
                            if (user != null) {
                                db.document("User/$userId")
                                    .update("fcmToken", SharedPref.getInstance(this).token)
                                    .addOnCompleteListener { tokenTask ->
                                        if (tokenTask.isSuccessful) {
                                            Toast.makeText(
                                                this@SignInActivity,
                                                "Registered for Notifications Successfully !",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@SignInActivity,
                                                "Registration for Notifications Failed !\nPlease Sign in Again to Retry",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                if (user.type == 0) {
                                    progressDialog.cancel()
                                    Toast.makeText(
                                        this@SignInActivity,
                                        "Signed in !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this@SignInActivity, UserHome::class.java))
                                    finish()
                                } else {
                                    progressDialog.cancel()
                                    Toast.makeText(
                                        this@SignInActivity,
                                        "Signed in !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this@SignInActivity, AdminHome::class.java))
                                    finish()
                                }
                            }
                        }
                } else {
                    progressDialog.cancel()
                    Toast.makeText(
                        this@SignInActivity,
                        "Wrong Credentials or Bad Connection ! Try Again ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }
}
