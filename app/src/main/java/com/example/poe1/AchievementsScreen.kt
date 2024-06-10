package com.example.poe1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun AchievementScreen(navController: NavHostController, userName: String) {
    val database = Firebase.database
    val userAchievementsRef = database.getReference("users").child(userName).child("achievements")

    var achievements by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    LaunchedEffect(Unit) {
        userAchievementsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val achievementsMap = mutableMapOf<String, Boolean>()
                for (achievementSnapshot in snapshot.children) {
                    val achievementName = achievementSnapshot.key ?: ""
                    val isAchieved = achievementSnapshot.getValue(Boolean::class.java) ?: false
                    achievementsMap[achievementName] = isAchieved
                }
                achievements = achievementsMap
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
        Text(text = "Achievements", fontSize = 24.sp, modifier = Modifier.padding(16.dp))

        for ((achievement, isAchieved) in achievements) {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = achievement, fontSize = 20.sp)
                    Text(text = if (isAchieved) "Achieved" else "Not Achieved")
                }
                Text(text = getAchievementDescription(achievement))
            }
        }
    }
}

fun getAchievementDescription(achievement: String): String {
    return when (achievement) {
        "Starter" -> "Add the first item to the app."
        "Collector" -> "Add three items to the app."
        "Packrat" -> "Add ten items to the app."
        else -> ""
    }
}
