package com.appsqueeze.librarymanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appsqueeze.librarymanagement.databinding.ActivitySearchBookSetBinding
import com.google.firebase.FirebaseApp

class SearchBookSet : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBookSetBinding
    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBookSetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupSpinner()

        binding.button3.setOnClickListener {
            if (!(verifyCategory() || verifyTitle() || verifyBid())) {
                Toast.makeText(
                    this@SearchBookSet,
                    "Select at least one parameter!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val intent = Intent(this@SearchBookSet, SearchBook::class.java).apply {
                when {
                    verifyBid() && binding.onlyAvailable.isChecked -> {
                        putExtra("id", 1)
                        putExtra("bid", binding.editBid3.editText?.text.toString().trim().toInt())
                    }
                    verifyBid() && !binding.onlyAvailable.isChecked -> {
                        putExtra("id", 2)
                        putExtra("bid", binding.editBid3.editText?.text.toString().trim().toInt())
                    }
                    verifyTitle() && verifyCategory() && binding.onlyAvailable.isChecked -> {
                        putExtra("id", 3)
                        putExtra("btitle", binding.editTitle3.editText?.text.toString().trim())
                        putExtra("btype", type)
                    }
                    verifyTitle() && verifyCategory() && !binding.onlyAvailable.isChecked -> {
                        putExtra("id", 4)
                        putExtra("btitle", binding.editTitle3.editText?.text.toString().trim())
                        putExtra("btype", type)
                    }
                    verifyTitle() && !verifyCategory() && binding.onlyAvailable.isChecked -> {
                        putExtra("id", 5)
                        putExtra("btitle", binding.editTitle3.editText?.text.toString().trim())
                    }
                    verifyTitle() && !verifyCategory() && !binding.onlyAvailable.isChecked -> {
                        putExtra("id", 6)
                        putExtra("btitle", binding.editTitle3.editText?.text.toString().trim())
                    }
                    !verifyTitle() && verifyCategory() && binding.onlyAvailable.isChecked -> {
                        putExtra("id", 7)
                        putExtra("btype", type)
                    }
                    !verifyTitle() && verifyCategory() && !binding.onlyAvailable.isChecked -> {
                        putExtra("id", 8)
                        putExtra("btype", type)
                    }
                }
            }
            startActivity(intent)
        }
    }

    private fun setupViews() {
        FirebaseApp.initializeApp(this)
    }

    private fun setupSpinner() {
        val A = resources.getStringArray(R.array.list1)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, A)
        binding.spinner3.adapter = adapter
        binding.spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                type = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: Handle nothing selected
            }
        }
    }

    private fun verifyTitle(): Boolean {
        val t = binding.editTitle3.editText?.text.toString().trim()
        return !t.isNullOrEmpty()
    }

    private fun verifyBid(): Boolean {
        val b = binding.editBid3.editText?.text.toString().trim()
        return !b.isNullOrEmpty()
    }

    private fun verifyCategory(): Boolean {
        return type != "Select Book Category"
    }
}
