//mainactivity
package com.mobile.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobile.compose.ui.screen.*
import com.mobile.compose.ui.theme.ComposeTheme
import com.mobile.compose.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                val navController = rememberNavController()
                AppNavigator(navController = navController)
            }
        }
    }
}

@Composable
fun AppNavigator(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val documentViewModel: DocumentViewModel = hiltViewModel()

    // Amati token dari repository untuk menentukan start destination
    val initialRoute = remember {
        if (authViewModel.repository.getToken() != null) "dashboard" else "login"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgotPassword = { navController.navigate("verifyEmail") },
                    onLoginSuccess = { navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } }
                )
            }
            composable("register") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onBack = { navController.popBackStack() },
                    onLoginClick = { navController.navigate("login") { popUpTo("register") { inclusive = true } } },
                    onSignUpSuccess = { email -> // Menerima email dari RegisterScreen
                        navController.navigate("otpVerification/$email") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }
            composable("otpVerification/{email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email")
                if (email != null) {
                    OTPEmailScreen(
                        viewModel = authViewModel,
                        email = email,
                        onVerified = { navController.navigate("login") { popUpTo("otpVerification/{email}") { inclusive = true } } }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
            composable("dashboard") {
                DashBoard(
                    viewModel = profileViewModel,
                    onLogout = { navController.navigate("login") { popUpTo("dashboard") { inclusive = true } } },
                    onEditProfile = { navController.navigate("ubahProfil") },
                    onUnggahDokumen = { navController.navigate("unggahDokumen") }
                )
            }
            composable("unggahDokumen") {
                UnggahDokumenScreen(
                    viewModel = documentViewModel,
                    onBack = { navController.popBackStack() },
                    onUploadSuccess = { navController.popBackStack() }
                )
            }
            composable("ubahProfil") {
                UbahProfilScreen(
                    viewModel = profileViewModel,
                    onClose = { navController.popBackStack() },
                    onSimpanSuccess = { navController.popBackStack() }
                )
            }
            composable("verifyEmail") {
                VerifyEmailScreen(
                    viewModel = authViewModel,
                    onBackToLogin = { navController.popBackStack() },
                    onLanjutToOtpEmail = { email -> // Menerima email dari VerifyEmailScreen
                        navController.navigate("otpPasswordReset/$email") {
                            popUpTo("verifyEmail") { inclusive = true }
                        }
                    }
                )
            }
            composable("otpPasswordReset/{email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email")
                if (email != null) {
                    OTPScreen(
                        viewModel = authViewModel,
                        email = email,
                        onVerified = { navController.navigate("forgotPassword") { popUpTo("otpPasswordReset/{email}") { inclusive = true } } }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
            composable("forgotPassword") {
                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onBackToLogin = { navController.navigate("login") { popUpTo("forgotPassword") { inclusive = true } } },
                    onSubmitSuccess = { navController.navigate("login") { popUpTo("forgotPassword") { inclusive = true } } }
                )
            }
        }
    }
}}
