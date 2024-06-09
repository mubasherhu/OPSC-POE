package com.example.poe1

import AddAndViewCategoryDetails
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.poe1.ui.theme.POE1Theme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            window.statusBarColor = getColor(R.color.black)
            val navController = rememberNavController()
            POE1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "signup") {
                        composable("signup") { SignUpScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("addCategory/{userName}") { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: return@composable
                            AddCategory(navController, userName)
                        }
                        composable("categoryDetails/{userName}/{categoryId}") { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: return@composable
                            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
                            AddAndViewCategoryDetails(navController, userName, categoryId)
                        }
                        composable("goalProgress/{userName}") { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: return@composable
                            GoalProgressScreen(navController, userName)
                        }
                        composable("achievements/{userName}") { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: return@composable
                            AchievementScreen(navController, userName)
                        }
                    }

                }
            }
        }
    }
}