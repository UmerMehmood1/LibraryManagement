package com.appsqueeze.librarymanagement

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class UserReissueBook : AppCompatActivity() {
    private var reissueButton2: Button? = null
    var spinner4: Spinner? = null
    private var db: FirebaseFirestore? = null
    var firebaseAuth: FirebaseAuth? = null
    private var p: ProgressDialog? = null
    private val res1 = false
    private var U = User()
    private var flag: String? = null
    private val A: MutableList<String?> = ArrayList()


    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_reissue_book)
        FirebaseApp.initializeApp(this)
        A.add("Select Book")
        reissueButton2 = findViewById(R.id.reissueButton2) as Button?
        spinner4 = findViewById(R.id.spinner4) as Spinner?
        db = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        p = ProgressDialog(this)
        p!!.setMessage("Please Wait !")

        p!!.show()
        db!!.document("User/" + firebaseAuth!!.currentUser?.email).get()
            .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot?> {
                override fun onComplete(@NonNull task: Task<DocumentSnapshot?>) {
                    if (task.isSuccessful) {
                        p!!.cancel()
                        U = task.result?.toObject(User::class.java)!!
                        var i = 0
                        while (i < U.book.size) {
                            if (U.re[i] == 1) A.add(U.book[i].toString())
                            i++
                        }
                    } else {
                        p!!.cancel()
                    }
                }
            })


        setSpinner4()


        spinner4?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                flag = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        reissueButton2!!.setOnClickListener { reissueBook() }
    }


    private fun setSpinner4() {
        val adapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                A as List<Any?>
            )
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinner4?.setAdapter(adapter)
    }

    private fun verifyCategory(): Boolean {
        if (flag == "Select Book") {
            Toast.makeText(this, "Please select a book to Re-issue !", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    private fun reissueBook() {
        if (verifyCategory()) return

        p?.show()

        var l: List<Int?> = ArrayList()

        val i = U.book.indexOf(flag!!.toInt())

        U.left_fine = (U.left_fine + U.fine[i])
        l = U.fine
        l.set(i, 0)
        U.fine = (l)

        l = U.re
        l.set(i, 0)
        U.re = (l)

        var l1: List<Timestamp?> = ArrayList<Timestamp?>()
        l1 = U.date
        var c: Calendar = object : Calendar() {
            override fun computeTime() {
            }

            override fun computeFields() {
            }

            override fun add(field: Int, amount: Int) {
            }

            override fun roll(field: Int, up: Boolean) {
            }

            override fun getMinimum(field: Int): Int {
                return 0
            }

            override fun getMaximum(field: Int): Int {
                return 0
            }

            override fun getGreatestMinimum(field: Int): Int {
                return 0
            }

            override fun getLeastMaximum(field: Int): Int {
                return 0
            }
        }
        c = Calendar.getInstance()
        val d = c.time
        val t: Timestamp = Timestamp(d)
        l1.set(i, t)
        U.date =(l1)


        db?.document("User/" + U.email)?.set(U)
            ?.addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    p?.cancel()
                    Toast.makeText(
                        this@UserReissueBook,
                        "Re-Issued Successfully !",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    p?.cancel()
                    Toast.makeText(
                        this@UserReissueBook,
                        "Please try Again !",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        A.remove(flag)
        setSpinner4()
        p?.cancel()
    }
}
