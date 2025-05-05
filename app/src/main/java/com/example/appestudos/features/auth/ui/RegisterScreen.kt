package com.example.appestudos.features.auth.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.appestudos.R
import com.example.appestudos.features.auth.viewmodel.AuthViewModel
import androidx.compose.ui.focus.focusRequester

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf(false) }
    val nomeFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val emailFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val senhaFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    fun isEmailValid(email: String): Boolean {
        return email.contains("@") && email.endsWith(".com")
    }

    Column(
        Modifier
            .fillMaxSize()
            .paint(
                painterResource(id = R.drawable.registerscreen_background),
                contentScale = ContentScale.FillWidth
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConstraintLayout(
            Modifier.fillMaxSize()
        ) {
            val (culm) = createRefs()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
                    .constrainAs(culm) {
                        bottom.linkTo(parent.bottom)
                    }
                    .background(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .padding(32.dp)
            ) {
                // Campo Nome
                Text(
                    text = "Nome:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                TextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome:") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { emailFocusRequester.requestFocus() }
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .focusRequester(nomeFocusRequester)
                )

                // Campo Email
                Text(
                    text = "Email:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Black
                )
                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = !isEmailValid(it)
                    },
                    label = { Text("Email:") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { senhaFocusRequester.requestFocus() }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    isError = emailError && email.isNotBlank(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Red
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .focusRequester(emailFocusRequester)
                )
                if (emailError && email.isNotBlank()) {
                    Text(
                        text = "Email inválido. Deve conter '@' e terminar com '.com'",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                // Campo Senha
                Text(
                    text = "Senha:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Black
                )
                TextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha:") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF5E5E5E)
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Red
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .focusRequester(senhaFocusRequester)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .weight(1f)
                            .background(Color(0xFF4D4D4D))
                    )
                }

                Button(
                    onClick = {
                        when {
                            nome.isBlank() || email.isBlank() || senha.isBlank() -> {
                                Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                            }
                            emailError -> {
                                Toast.makeText(context, "Email inválido. Deve conter '@' e terminar com .com", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                authViewModel.register(
                                    nome = nome,
                                    email = email,
                                    senha = senha
                                ) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) navController.navigate("LoginScreen")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Cadastrar",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(navController = rememberNavController())
}
