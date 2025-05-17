import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navToSignIn: () -> Unit,
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    val db = Firebase.firestore

    val darkBackground = Color(0xFF121212)
    val textFieldBorder = Color(0xFF2A2A2A)
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
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navToSignIn() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Sign In",
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                val logo = painterResource(id = R.drawable.logo)
                Image(
                    painter = logo,
                    contentDescription = "Logo",
                    modifier = Modifier.size(20.dp)
                )
            }

            val poppinsLight = FontFamily(
                Font(R.font.poppins_light, FontWeight.Light)
            )
            val poppinsSemiBold = FontFamily(
                Font(R.font.poppins_semibold, FontWeight.SemiBold)
            )

            Text(
                text = "Create your account",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = TextAlign.Center,
                fontFamily = poppinsSemiBold
            )

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("ex: jon smith", color = hintColor, fontFamily = poppinsLight) },
                label = { Text("Name", color = textColor, fontFamily = poppinsLight) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color(0xFF5A9CB5),
                    focusedBorderColor = Color(0xFF5A9CB5),
                    unfocusedBorderColor = textFieldBorder
                ),
                textStyle = LocalTextStyle.current.copy(color = textColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("ex: jon.smith@email.com", color = hintColor, fontFamily = poppinsLight) },
                label = { Text("Email", color = textColor, fontFamily = poppinsLight) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color(0xFF5A9CB5),
                    focusedBorderColor = Color(0xFF5A9CB5),
                    unfocusedBorderColor = textFieldBorder
                ),
                textStyle = LocalTextStyle.current.copy(color = textColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Password field
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                placeholder = { Text("••••••••", color = hintColor, fontFamily = poppinsLight) },
                label = { Text("Password", color = textColor, fontFamily = poppinsLight) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color(0xFF5A9CB5),
                    focusedBorderColor = Color(0xFF5A9CB5),
                    unfocusedBorderColor = textFieldBorder
                ),
                textStyle = LocalTextStyle.current.copy(color = textColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Confirm Password field
            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it },
                placeholder = { Text("••••••••", color = hintColor, fontFamily = poppinsLight) },
                label = { Text("Confirm Password", color = textColor, fontFamily = poppinsLight) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color(0xFF5A9CB5),
                    focusedBorderColor = Color(0xFF5A9CB5),
                    unfocusedBorderColor = textFieldBorder
                ),
                textStyle = LocalTextStyle.current.copy(color = textColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    if (pass != confirmPass) {
                        Toast.makeText(ctx, "Passwords do not match", Toast.LENGTH_LONG).show()
                    } else {
                        auth.createUserWithEmailAndPassword(email.trim(), pass)
                            .addOnSuccessListener { result ->
                                val uid = result.user?.uid ?: return@addOnSuccessListener
                                val profile = mapOf(
                                    "displayName" to name,
                                    "email" to email.trim(),
                                    "createdAt" to System.currentTimeMillis(),
                                    "timeZone" to "Africa/Tunis"
                                )
                                db.collection("users").document(uid)
                                    .set(profile)
                                    .addOnSuccessListener { navToSignIn() }
                                    .addOnFailureListener { ex ->
                                        Log.e("SignUp", "createUser failed", ex)
                                        Toast.makeText(ctx, ex.localizedMessage ?: "Unknown error", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { ex ->
                                Toast.makeText(ctx, ex.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(4.dp)), // Sharper corners
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5A9CB5)
                )
            ) {
                Text(
                    "SIGN UP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppinsSemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(
                        color = textColor,
                        fontSize = 14.sp,
                        fontFamily = poppinsLight
                    )) {
                        append("Have an account? ")
                    }
                    withStyle(style = SpanStyle(
                        color = Color(0xFF5A9CB5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppinsLight
                    )) {
                        append("SIGN IN")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navToSignIn() }
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
