package com.esime.ubicacionmaestra.Firstapp.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esime.ubicacionmaestra.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity2 : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    companion object {
        const val TAG = "MainActivity" // Definimos la variable TAG aqui
    }

    val db = FirebaseFirestore.getInstance()
    val user = hashMapOf(
        "Latitud" to "-",
        "Logitud" to "-",
        "email" to "@",
        "pass" to "-",
        "Nombre" to "-"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        /*crearDocument.setOnClickListener {

        }*/
    }
    fun crearcolletion(){

        // Add a new document with a generated ID

        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
}


