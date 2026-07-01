package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

// High-Fidelity Colors matching Bekansi Luxury Branding
private val DeepPurple = Color(0xFF130026)
private val WarmMahogany = Color(0xFF4A1E1E)
private val GoldAccent = Color(0xFFCCA43B)
private val LightWarmCard = Color(0xFFF3EDE4)
private val DarkGlass = Color(0xFF231435)

@Composable
fun AuthScreen(
    onLoginSuccess: (email: String, role: String) -> Unit
) {
    val context = LocalContext.current
    var isSignUpMode by remember { mutableStateOf(false) }
    var isForgotPasswordMode by remember { mutableStateOf(false) }

    // State bindings
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var companyCode by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Sales Representative") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Luxury Background Overlay
        Image(
            painter = painterResource(id = R.drawable.img_bekansi_bg),
            contentDescription = "Auth Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepPurple.copy(alpha = 0.95f),
                            DeepPurple.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Main Login Container
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGlass.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 24.dp)
                    .testTag("auth_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo and Brand Name
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🇪🇹",
                                fontSize = 32.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "BEKANSI AI",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Furniture & Interior Design (Ethiopia)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Divider(color = GoldAccent.copy(alpha = 0.3f), thickness = 1.dp)

                    // Form Mode Navigation Title
                    Text(
                        text = when {
                            isForgotPasswordMode -> "Reset Security Password"
                            isSignUpMode -> "Create Business Account"
                            else -> "Employee Secure Workstation Login"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    if (!isForgotPasswordMode) {
                        // User Role Chooser (Required for Role-Based Access)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Workstation Role Profile",
                                fontSize = 11.sp,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            val roles = listOf("Sales Representative", "Interior Designer", "Administrator")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                roles.forEach { role ->
                                    val isSelected = selectedRole == role
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedRole = role },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) WarmMahogany else DeepPurple.copy(alpha = 0.5f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.15f)
                                        )
                                    ) {
                                        Text(
                                            text = role.substringBefore(" "),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Input Form Fields
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Corporate Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldAccent) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = GoldAccent,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                            )
                        )

                        if (!isForgotPasswordMode) {
                            // Password Field
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Access Security Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.PlayArrow else Icons.Default.Lock,
                                            contentDescription = if (showPassword) "Hide Password" else "Show Password",
                                            tint = GoldAccent
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = GoldAccent,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )
                        }

                        if (isSignUpMode && !isForgotPasswordMode) {
                            // Confirm Password
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Security Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = GoldAccent,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )

                            // Company Invitation Code
                            OutlinedTextField(
                                value = companyCode,
                                onValueChange = { companyCode = it },
                                label = { Text("Bekansi Company Auth Code") },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldAccent) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = GoldAccent,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }

                    // Bottom Navigation triggers (Forgot Pass / Sign up toggle)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isForgotPasswordMode) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 11.sp,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { isForgotPasswordMode = true }
                                    .padding(vertical = 4.dp)
                            )
                            Text(
                                text = if (isSignUpMode) "Sign In Instead" else "Register Access",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { isSignUpMode = !isSignUpMode }
                                    .padding(vertical = 4.dp)
                            )
                        } else {
                            Text(
                                text = "Back to Sign In",
                                fontSize = 11.sp,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        isForgotPasswordMode = false
                                        isSignUpMode = false
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // PRIMARY ACTION BUTTON (With visual loader)
                    Button(
                        onClick = {
                            if (email.isBlank() || (!isForgotPasswordMode && password.isBlank())) {
                                Toast.makeText(context, "Please complete all fields.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (isSignUpMode && password != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (isSignUpMode && companyCode != "BEKANSI-2026") {
                                Toast.makeText(context, "Invalid corporate invitation code.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            // Simulate high-fidelity network authentication
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isLoading = false
                                if (isForgotPasswordMode) {
                                    Toast.makeText(context, "A password recovery email has been dispatched to $email", Toast.LENGTH_LONG).show()
                                    isForgotPasswordMode = false
                                } else {
                                    val userMail = if (email.contains("@")) email else "$email@bekansi.com"
                                    Toast.makeText(context, "Welcome, $userMail ($selectedRole)!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(userMail, selectedRole)
                                }
                            }, 1200)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = DeepPurple, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = when {
                                    isForgotPasswordMode -> "Send Recovery Link"
                                    isSignUpMode -> "Register Workstation Access"
                                    else -> "Secure Login 🔐"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepPurple
                            )
                        }
                    }

                    // Google Authentication Simulator Option
                    if (!isForgotPasswordMode && !isSignUpMode) {
                        Text(
                            text = "— OR —",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )

                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    isLoading = false
                                    Toast.makeText(context, "Google Sign-In Approved!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess("salam.bekansi@gmail.com", selectedRole)
                                }, 1000)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🔴 ", fontSize = 14.sp)
                                Text(
                                    text = "Continue with Google Identity",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
