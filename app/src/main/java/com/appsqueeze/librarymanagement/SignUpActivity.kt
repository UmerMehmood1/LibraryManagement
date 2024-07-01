package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.appsqueeze.librarymanagement.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var type: String? = null
    private var type1: Int = 0
    private var temp: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val list = mutableListOf<String>()
        list.add("Select Account Type")
        list.add("User")
        list.add("Admin")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
        binding.userType.adapter = adapter

        binding.userType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (parent?.getItemAtPosition(position).toString()) {
                    "Select Account Type" -> {
                        type = null
                        disableFields()
                    }
                    "User" -> {
                        type = "User"
                        enableUserFields()
                    }
                    "Admin" -> {
                        type = "Admin"
                        enableAdminFields()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.buttonRegister.setOnClickListener(this)
        binding.toSignIn.setOnClickListener(this)
        binding.check1.setOnClickListener(this)
    }

    private fun enableUserFields() {
        with(binding) {
            editPass1.isEnabled = true
            editPass.isEnabled = true
            editName.isEnabled = true
            editID.isEnabled = true
            editEnrollNo.isEnabled = true
            editCardNo.isEnabled = true

            clearErrors()
        }
    }

    private fun enableAdminFields() {
        with(binding) {
            editPass1.isEnabled = true
            editPass.isEnabled = true
            editName.isEnabled = true
            editID.isEnabled = true
            editEnrollNo.isEnabled = false
            editCardNo.isEnabled = false

            clearErrors()
        }
    }

    private fun disableFields() {
        with(binding) {
            editPass1.isEnabled = false
            editPass.isEnabled = false
            editName.isEnabled = false
            editID.isEnabled = false
            editEnrollNo.isEnabled = false
            editCardNo.isEnabled = false

            clearErrors()
        }
    }

    private fun clearErrors() {
        with(binding) {
            editCardNo.error = null
            editEnrollNo.error = null
            editID.error = null
            editName.error = null
            editPass.error = null
            editPass1.error = null
        }
    }

    private fun verifyName(): Boolean {
        val name = binding.editName.editText?.text.toString().trim()
        return if (name.isEmpty()) {
            binding.editName.error = "Name Required"
            true
        } else {
            binding.editName.error = null
            false
        }
    }

    private fun verifyCardNo(): Boolean {
        val cardNo = binding.editCardNo.editText?.text.toString().trim()
        return if (cardNo.isEmpty()) {
            binding.editCardNo.error = "Card No. Required"
            true
        } else {
            binding.editCardNo.error = null
            false
        }
    }

    private fun verifyEnrollNo(): Boolean {
        val enrollNo = binding.editEnrollNo.editText?.text.toString().trim()
        return if (enrollNo.isEmpty()) {
            binding.editEnrollNo.error = "Enrollment No. Required"
            true
        } else {
            binding.editEnrollNo.error = null
            false
        }
    }

    private fun verifyEmailId(): Boolean {
        val emailId = binding.editID.editText?.text.toString().trim()
        return when {
            emailId.isEmpty() -> {
                binding.editID.error = "Email ID Required"
                true
            }
            else -> {
                binding.editID.error = null
                false
            }
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

    private fun verifyPass1(): Boolean {
        val pass1 = binding.editPass1.editText?.text.toString().trim()
        val pass = binding.editPass.editText?.text.toString().trim()
        return when {
            pass1.isEmpty() -> {
                binding.editPass1.error = "Confirm Password Required"
                true
            }
            pass != pass1 -> {
                binding.editPass1.error = "Passwords do not match"
                true
            }
            else -> {
                binding.editPass1.error = null
                false
            }
        }
    }

    private fun verifyType(): Boolean {
        if (type == null || type == "Select Account Type") {
            Toast.makeText(this, "Please select account type !", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    private fun verifyCard1(): Boolean {
        db.collection("User")
            .whereEqualTo("card", binding.editCardNo.editText?.text.toString().trim().toInt())
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    temp = task.result?.size() ?: 0
                }
            }
        return temp != 0
    }

    private fun registerUser() {
        if (verifyType()) return

        if (type == "User") {
            val res = (verifyName() || verifyCardNo() || verifyEmailId() || verifyEnrollNo() || verifyPass() || verifyPass1())
            if (res) return
        } else if (type == "Admin") {
            val res = (verifyName() || verifyEmailId() || verifyPass() || verifyPass1())
            if (res) return
        }

        val id = binding.editID.editText?.text.toString().trim()
        val pass = binding.editPass.editText?.text.toString().trim()
        type1 = if (type == "User") {
            0
        } else {
            1
        }

        progressDialog.setMessage("Registering User ... ")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(id, pass)
            .addOnCompleteListener(this@SignUpActivity) { task ->
                if (task.isSuccessful) {
                    val name = binding.editName.editText?.text.toString().trim()
                    if (type1 == 0) {
                        val enroll = binding.editEnrollNo.editText?.text.toString().trim().toInt()
                        val card = binding.editCardNo.editText?.text.toString().trim().toInt()
                        db.collection("User").document(id)
                            .set(User(name = name, Id = id, varenroll = enroll, card = card, type = type1))
                            .addOnSuccessListener {
                                progressDialog.cancel()
                                Toast.makeText(this@SignUpActivity, "Registered Successfully !", Toast.LENGTH_SHORT).show()
                                firebaseAuth.signOut()
                                startActivity(Intent(applicationContext, SignInActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@SignUpActivity, "Please Try Again !", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        db.collection("User").document(id).set(Admin(type1, name, id))
                            .addOnSuccessListener {
                                progressDialog.cancel()
                                Toast.makeText(this@SignUpActivity, "Registered Successfully !", Toast.LENGTH_SHORT).show()
                                firebaseAuth.signOut()
                                startActivity(Intent(applicationContext, SignInActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@SignUpActivity, "Please Try Again !", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    progressDialog.cancel()
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this@SignUpActivity, "Already Registered ! ", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SignUpActivity, "Registration Failed ! Try Again ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.check1 -> binding.buttonRegister.isEnabled = binding.check1.isChecked
            R.id.buttonRegister -> registerUser()
            R.id.toSignIn -> startActivity(Intent(applicationContext, SignInActivity::class.java))
        }
    }
}
