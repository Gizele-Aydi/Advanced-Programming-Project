import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.setValue         // ← needed for `by` delegation
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

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

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (pass != confirmPass) {
                    Toast.makeText(ctx, "Passwords do not match", Toast.LENGTH_LONG).show()
                } else {
                    auth.createUserWithEmailAndPassword(email.trim(), pass)
                        .addOnSuccessListener { result ->
                            // Write user profile to Firestore
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
                                .addOnFailureListener { e ->
                                    Toast.makeText(ctx, e.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { ex ->
                            Toast.makeText(ctx, ex.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                append("Already have an account? ")
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append("Sign In")
                }
            },
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable { navToSignIn() }
        )
    }
}