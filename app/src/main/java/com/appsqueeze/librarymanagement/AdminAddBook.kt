package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivityAdminAddBookBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class AdminAddBook : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAdminAddBookBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog
    private var bookType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        progressDialog = createProgressDialog()
        firestore = FirebaseFirestore.getInstance()

        setupSpinner()
        setupButton()
        handleOnBackPressed()
    }

    private fun createProgressDialog(): ProgressDialog {
        return ProgressDialog(this).apply {
            setCancelable(false)
        }
    }

    private fun setupSpinner() {
        val categories: Array<String> = resources.getStringArray(R.array.list1)
        val adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, categories)
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        binding.spinner1.adapter = adapter
        binding.spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                bookType = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupButton() {
        binding.button1.setOnClickListener(this)
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun verifyTitle(): Boolean {
        val title = binding.editTitle.editText?.text.toString().trim()
        return if (title.isEmpty()) {
            binding.editTitle.error = "Title Required"
            true
        } else {
            binding.editTitle.isErrorEnabled = false
            false
        }
    }

    private fun verifyBookId(): Boolean {
        val bookId = binding.editBid.editText?.text.toString().trim()
        return if (bookId.isEmpty()) {
            binding.editBid.error = "Book Id Required"
            true
        } else {
            binding.editBid.isErrorEnabled = false
            false
        }
    }

    private fun verifyUnits(): Boolean {
        val units = binding.editUnits.editText?.text.toString().trim()
        return if (units.isEmpty()) {
            binding.editUnits.error = "No. of Units Required"
            true
        } else {
            binding.editUnits.isErrorEnabled = false
            false
        }
    }

    private fun verifyCategory(): Boolean {
        return if (bookType == "Select Book Category") {
            showToast("Please select Book Category!")
            true
        } else {
            false
        }
    }

    private fun addBook() {
        if (verifyBookId() or verifyTitle() or verifyUnits() or verifyCategory()) return

        showProgressDialog("Adding Book")

        val bookId = binding.editBid.editText?.text.toString().trim()
        val bookIdInt = bookId.toInt()
        firestore.document("Book/$bookIdInt").get()
            .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot?> {
                override fun onComplete(@NonNull task: Task<DocumentSnapshot?>) {
                    if (task.isSuccessful && task.result?.exists() == false) {
                        saveBook(bookIdInt)
                    } else {
                        hideProgressDialog()
                        showToast("This Book is already added \n or Bad Connection!")
                    }
                }
            })
    }

    private fun saveBook(bookId: Int) {
        val title = binding.editTitle.editText?.text.toString().trim().uppercase(Locale.getDefault())
        val units = binding.editUnits.editText?.text.toString().trim().toInt()
        val book = Book(title = title, type = bookType!!, total = units, available = units, id = bookId)

        firestore.document("Book/$bookId").set(book)
            .addOnCompleteListener(object : OnCompleteListener<Void?> {
                override fun onComplete(@NonNull task: Task<Void?>) {
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        showToast("Book Added!")
                    } else {
                        showToast("Try Again!")
                    }
                }
            })
    }

    private fun showProgressDialog(message: String) {
        progressDialog.setMessage(message)
        progressDialog.show()
    }

    private fun hideProgressDialog() {
        progressDialog.cancel()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View) {
        addBook()
    }
}
