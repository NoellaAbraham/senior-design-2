package com.example.sd2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class Registration : ComponentActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextAge: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var spinnerUserType: Spinner
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        spinnerUserType = findViewById(R.id.spinnerUserType)
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextAge = findViewById(R.id.editTextAge)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonRegister = findViewById(R.id.buttonNext)
        requestQueue = Volley.newRequestQueue(this)

        ArrayAdapter.createFromResource(
            this,
            R.array.user_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUserType.adapter = adapter
        }

        buttonRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = editTextName.text.toString()
        val age = editTextAge.text.toString().toIntOrNull() ?: 0
        val email = editTextEmail.text.toString()
        val userType = spinnerUserType.selectedItem.toString()
        val password = editTextPassword.text.toString()

        val url = "http://192.168.56.1/seniordes/register.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val obj = JSONObject(response)
                    Toast.makeText(applicationContext, obj.getString("message"), Toast.LENGTH_LONG).show()
                    goToNextActivity(this, MainActivity::class.java)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { volleyError ->
                volleyError.printStackTrace()
                Toast.makeText(applicationContext, "Error: ${volleyError.message}", Toast.LENGTH_LONG).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["name"] = name
                params["age"] = age.toString()
                params["usertype"] = userType
                params["email"] = email
                params["password"] = password
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
