package com.example.moodify.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.moodify.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    navToSignUp: () -> Unit,
    navToHome: () -> Unit,
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    val db = Firebase.firestore

    // Define colors to match the design
    val darkBackground = Color(0xFF121212)
    val textFieldBorder = Color(0xFF2A2A2A)
    val primaryBlue = Color(0xFF3B82F6)
    val textColor = Color.White
    val hintColor = Color(0xFF6B7280)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            val logo = painterResource(id = R.drawable.logo)

            Image(
                painter = logo,
                contentDescription = "Logo",
                modifier = Modifier.size(50.dp)
            )


            Spacer(modifier = Modifier.height(40.dp))

            val poppinsLight = FontFamily(
                Font(R.font.poppins_light, FontWeight.Light)
            )
            val poppinsSemiBold = FontFamily(
                Font(R.font.poppins_semibold, FontWeight.SemiBold)
            )

            Text(
                "SIGN IN YOUR ACCOUNT",
                fontFamily = poppinsSemiBold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("ex: jon.smith@email.com", color = hintColor, fontFamily = poppinsLight) },
                label = { Text("Email", color = textColor, fontFamily = poppinsLight) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = primaryBlue,
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = textFieldBorder
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                placeholder = { Text("••••••••", color = hintColor, fontFamily = poppinsLight) },
                label = { Text("Password", color = textColor, fontFamily = poppinsLight) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = primaryBlue,
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = textFieldBorder
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Sign In button
            Button(
                onClick = {
                    auth.signInWithEmailAndPassword(email.trim(), pass)
                        .addOnSuccessListener { result ->
                            // Optional: update lastLogin timestamp
                            val uid = result.user?.uid ?: return@addOnSuccessListener
                            db.collection("users").document(uid)
                                .update("lastLogin", System.currentTimeMillis())
                            navToHome()
                        }
                        .addOnFailureListener { ex ->
                            Toast.makeText(ctx, ex.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5A9CB5)
                )
            ) {
                Text(
                    "SIGN IN",
                    fontFamily = poppinsSemiBold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up text
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(
                        fontFamily = poppinsLight,
                        color = textColor,
                        fontSize = 14.sp
                    )) {
                        append("Don't have an account? ")
                    }
                    withStyle(style = SpanStyle(
                        fontFamily = poppinsLight,
                        color = Color(0xFF5A9CB5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )) {
                        append("SIGN UP")
                    }
                },
                modifier = Modifier
                    .clickable { navToSignUp() }
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
