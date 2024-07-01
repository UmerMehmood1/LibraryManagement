package com.appsqueeze.librarymanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivityUserHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserHome : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityUserHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.apply {
            title1 // Access directly using ViewBinding
            searchBook1.setOnClickListener(this@UserHome)
            seeBook.setOnClickListener(this@UserHome)
            logOut1.setOnClickListener(this@UserHome)
            buttonReissue.setOnClickListener(this@UserHome)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.logOut1.id -> logoutUser()
            binding.searchBook1.id -> startActivity(Intent(applicationContext, SearchBookSet::class.java))
            binding.seeBook.id -> startActivity(Intent(applicationContext, UserSeeMyBooks::class.java))
            binding.buttonReissue.id -> startActivity(Intent(applicationContext, UserReissueBook::class.java))
        }
    }

    private fun logoutUser() {
        db.document("User/${firebaseAuth.currentUser?.email}")
            .update("fcmToken", null)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseAuth.signOut()
                    startActivity(Intent(applicationContext, SignInActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@UserHome, "Try Again !", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
