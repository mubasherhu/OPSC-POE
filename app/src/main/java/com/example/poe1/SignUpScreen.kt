package com.example.poe1

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
@Composable
fun SignUpScreen(navController: NavHostController) {
    val database = Firebase.database
    val usersRef = database.getReference("users")

    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sign Up", fontSize = 45.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(4.dp))

        Text(text = "Sign Into your account", fontSize = 15.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { newText -> email = newText },
            label = { Text(text = "Email") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = { newText -> userName = newText },
            label = { Text(text = "Username") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { newText -> password = newText },
            label = { Text(text = "Password") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        Button(onClick = {
            // Validate input fields
            when {
                email.isBlank() || userName.isBlank() || password.isBlank() -> {
                    Toast.makeText(context, "All fields must be filled out", Toast.LENGTH_SHORT).show()
                }
                !email.endsWith("@gmail.com") -> {
                    Toast.makeText(context, "Email must be a valid @gmail.com address", Toast.LENGTH_SHORT).show()
                }
                password.length < 4 -> {
                    Toast.makeText(context, "Password must be at least 4 characters long", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Create a User object with the input data
                    val user = User(email, password)

                    // Save the user data to the Firebase database
                    usersRef.child(userName).setValue(user)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // If successful, display a success message and navigate to the login screen
                                Toast.makeText(context, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                                // Clear the input fields
                                userName = ""
                                email = ""
                                password = ""
                                // Navigate to the login screen
                                navController.navigate("login")
                            } else {
                                // If failed, display an error message
                                Toast.makeText(context, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }) {
            // Text on the Sign Up button
            Text(text = "Sign Up")
        }


        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Already have an account? Login",
            modifier = Modifier.clickable {
                navController.navigate("login")
            }
        )
    }
}

data class User(val email: String = "", val password: String = "")
