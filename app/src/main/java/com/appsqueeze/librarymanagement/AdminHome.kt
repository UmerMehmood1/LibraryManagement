package com.appsqueeze.librarymanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivityAdminHomeBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminHome : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAdminHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        with(binding) {
            searchBook.setOnClickListener(this@AdminHome)
            addBook.setOnClickListener(this@AdminHome)
            removeBook.setOnClickListener(this@AdminHome)
            updateBook.setOnClickListener(this@AdminHome)
            issueBook.setOnClickListener(this@AdminHome)
            returnBook.setOnClickListener(this@AdminHome)
            logOut.setOnClickListener(this@AdminHome)
            collect1.setOnClickListener(this@AdminHome)
            reissueBook.setOnClickListener(this@AdminHome)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.logOut.id -> handleLogout()
            binding.searchBook.id -> navigateToActivity(SearchBookSet::class.java)
            binding.addBook.id -> navigateToActivity(AdminAddBook::class.java)
            binding.removeBook.id -> navigateToActivity(AdminRemoveBook::class.java)
            binding.updateBook.id -> navigateToActivity(AdminUpdateBook::class.java)
            binding.issueBook.id -> navigateToActivity(AdminIssueBook::class.java)
            binding.returnBook.id -> navigateToActivity(AdminReturnBook::class.java)
            binding.collect1.id -> navigateToActivity(AdminCollectFine::class.java)
            binding.reissueBook.id -> navigateToActivity(AdminReissueBook::class.java)
        }
    }

    private fun handleLogout() {
        firebaseAuth.currentUser?.email?.let { email ->
            firestore.document("User/$email").update("fcmToken", null)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseAuth.signOut()
                        startActivity(Intent(applicationContext, SignInActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(applicationContext, activityClass))
    }
}
