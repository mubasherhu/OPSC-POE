package com.example.poe1

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun AddCategory(navController: NavHostController, userName: String) {
    val database = Firebase.database
    val userCategoriesRef = database.getReference("users").child(userName).child("categories")
    val userAchievementsRef = database.getReference("users").child(userName).child("achievements")

    var categoryName by remember { mutableStateOf("") }
    val context = LocalContext.current
    var categories by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    fun updateAchievements(categoryCount: Int) {
        when (categoryCount) {
            1 -> userAchievementsRef.child("Starter").setValue(true)
            3 -> userAchievementsRef.child("Collector").setValue(true)
            10 -> userAchievementsRef.child("Packrat").setValue(true)
        }
        when(categoryCount){
            1-> Toast.makeText(context,"Achievement: Starter", Toast.LENGTH_LONG).show()
            3-> Toast.makeText(context,"Achievement: Starter", Toast.LENGTH_LONG).show()
            10-> Toast.makeText(context,"Achievement: Starter", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        userCategoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = mutableListOf<Pair<String, String>>()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.child("name").getValue(String::class.java)
                    val categoryId = categorySnapshot.key
                    if (category != null && categoryId != null) {
                        categoryList.add(Pair(categoryId, category))
                    }
                }
                categories = categoryList
                updateAchievements(categoryList.size)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Add Category", fontSize = 45.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = categoryName,
            onValueChange = { newText -> categoryName = newText },
            label = { Text(text = "Category Name") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (categoryName.isNotEmpty()) {
                val categoryId = userCategoriesRef.push().key
                if (categoryId != null) {
                    val category = Category(categoryName)
                    userCategoriesRef.child(categoryId).setValue(category)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Category Added Successfully", Toast.LENGTH_SHORT).show()
                                categoryName = ""
                            } else {
                                Toast.makeText(context, "Failed to Add Category: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(context, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Add Category")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Select a Category to Add Details")

        Spacer(modifier = Modifier.height(16.dp))

        for ((categoryId, category) in categories) {
            Text(
                text = category,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("categoryDetails/$userName/$categoryId")
                    }
                    .padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            navController.navigate("goalProgress/$userName")
        }) {
            Text(text = "View Goal Progress")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate("achievements/$userName")
        }) {
            Text(text = "View Achievements")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            navController.navigate("login")
        }) {
            Text(text = "Logout")
        }
    }
}

data class Category(val name: String = "")
