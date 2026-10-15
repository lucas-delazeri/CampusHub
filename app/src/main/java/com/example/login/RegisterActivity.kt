package com.example.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.ui.theme.LoginTheme
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class RegisterActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            LoginTheme {
                RegisterScreen { email, password, role ->
                    registerUser(email, password, role)
                }
            }
        }
    }

    private fun registerUser(email: String, password: String, role: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        com.google.firebase.database.FirebaseDatabase.getInstance().reference
                            .child("users").child(userId).child("role")
                            .setValue(role)
                    }
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Falha na criação de usuário: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Composable
fun RegisterScreen(onRegister: (String, String, String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
    ) {
        RegisterContent(onRegister)
    }
}

@Composable
fun RegisterContent(onRegister: (String, String, String) -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("aluno") }
    var validEmail by remember { mutableStateOf(true) }
    var validPassword by remember { mutableStateOf(true) }
    var passwordsMatch by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.app_img),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Crie sua conta",
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Cadastre-se e começe a explorar",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        RegisterEmailField(
            email = email,
            isValid = validEmail,
            onValueChange = {
                email = it
                validEmail = isValidEmail(it)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        RegisterPasswordField(
            password = password,
            isValid = validPassword,
            onValueChange = {
                password = it
                validPassword = isValidPassword(it)
                passwordsMatch = confirmPassword.isEmpty() || confirmPassword == password
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ConfirmPasswordField(
            confirmPassword = confirmPassword,
            isValid = passwordsMatch,
            onValueChange = {
                confirmPassword = it
                passwordsMatch = it == password
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tipo de Conta",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { selectedRole = "aluno" },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedRole == "aluno") Color(0xFF0F93FF) else Color(0xFFE2E8F0)
                )
            ) {
                Text(
                    text = "Aluno",
                    color = if (selectedRole == "aluno") Color.White else Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { selectedRole = "professor" },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedRole == "professor") Color(0xFF0F93FF) else Color(0xFFE2E8F0)
                )
            ) {
                Text(
                    text = "Professor",
                    color = if (selectedRole == "professor") Color.White else Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        RegisterButton {
            val emailOk = isValidEmail(email)
            val passOk = isValidPassword(password)
            val matchOk = password == confirmPassword

            validEmail = emailOk
            validPassword = passOk
            passwordsMatch = matchOk

            if (emailOk && passOk && matchOk) {
                onRegister(email, password, selectedRole)
            }
        }
    }
}

@Composable
fun RegisterEmailField(email: String, isValid: Boolean, onValueChange: (String) -> Unit) {
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = onValueChange,
            placeholder = { Text("Email", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF673AB7),
                unfocusedBorderColor = Color.LightGray,
                errorBorderColor = Color.Red
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = !isValid,
            singleLine = true
        )
        if (!isValid) {
            Text(
                text = "Por favor insira um email válido!",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun RegisterPasswordField(password: String, isValid: Boolean, onValueChange: (String) -> Unit) {
    Column {
        OutlinedTextField(
            value = password,
            onValueChange = onValueChange,
            placeholder = { Text("Senha", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF673AB7),
                unfocusedBorderColor = Color.LightGray,
                errorBorderColor = Color.Red
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = !isValid,
            singleLine = true
        )
        if (!isValid) {
            Text(
                text = "A senha precisa ter 8+ caracteres, uma letra maiúscula e um número",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ConfirmPasswordField(confirmPassword: String, isValid: Boolean, onValueChange: (String) -> Unit) {
    Column {
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onValueChange,
            placeholder = { Text("Confirmar senha", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF673AB7),
                unfocusedBorderColor = Color.LightGray,
                errorBorderColor = Color.Red
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = !isValid,
            singleLine = true
        )
        if (!isValid) {
            Text(
                text = "Insira a mesma senha anterior!",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun RegisterButton(onClicked: () -> Unit) {
    Button(
        onClick = onClicked,
        modifier = Modifier
            .width(150.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F93FF)),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = "Cadastre-se",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun isValidEmail(email: String): Boolean {
    val emailTemplate = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
    val template = Pattern.compile(emailTemplate)
    val matcher = template.matcher(email)
    return matcher.matches()
}

fun isValidPassword(password: String): Boolean {
    val passwordTemplate = "^(?=.*[A-Z])(?=.*\\d).{8,}\$"
    val template = Pattern.compile(passwordTemplate)
    val matcher = template.matcher(password)
    return matcher.matches()
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    LoginTheme {
        RegisterScreen { _, _, _ -> }
    }
}