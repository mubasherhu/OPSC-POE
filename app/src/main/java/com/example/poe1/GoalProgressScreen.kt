package com.example.poe1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun GoalProgressScreen(navController: NavHostController, userName: String) {
    val database = Firebase.database
    val userCategoriesRef = database.getReference("users").child(userName).child("categories")
    val userAchievementsRef = database.getReference("users").child(userName).child("achievements")

    var categoriesProgress by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        userCategoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progressMap = mutableMapOf<String, Int>()
                var totalItems = 0

                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.child("name").getValue(String::class.java) ?: ""
                    val detailsCount = categorySnapshot.child("details").childrenCount.toInt()
                    progressMap[categoryName] = detailsCount
                    totalItems += detailsCount
                }

                categoriesProgress = progressMap

                // Update achievements based on totalItems count
                val achievements = mutableMapOf<String, Boolean>()
                if (totalItems >= 1) achievements["Starter"] = true else achievements["Starter"] = false
                if (totalItems >= 3) achievements["Collector"] = true else achievements["Collector"] = false
                if (totalItems >= 10) achievements["Packrat"] = true else achievements["Packrat"] = false

                userAchievementsRef.setValue(achievements)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Your Goals Progress", fontSize = 24.sp, modifier = Modifier.padding(16.dp))

        for ((category, progress) in categoriesProgress) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = category, fontSize = 20.sp)
                CircularProgressIndicator(progress = progress / 10f)
                Text(text = "$progress/10 items added")
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

