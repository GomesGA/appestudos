package com.example.appestudos.features.auth.ui

import android.widget.Toast
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
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.appestudos.features.auth.viewmodel.AuthViewModel
import com.example.appestudos.R
import androidx.compose.foundation.text.KeyboardActions


@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    var text2 by rememberSaveable { mutableStateOf("") }
    var passwordFieldFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    var emailError by rememberSaveable { mutableStateOf(false) }
    val emailFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val senhaFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    fun isEmailValid(email: String): Boolean {
        return email.contains("@") && email.endsWith(".com")
    }

    // Função para navegar para a tela inicial após login bem-sucedido
    fun navigateToHome() {
        navController.navigate("HomeScreen") {
            // Remove todas as telas anteriores da pilha
            popUpTo("LoginScreen") { inclusive = true }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .paint(
                painterResource(id = R.drawable.background_page),
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConstraintLayout(
            Modifier
                .fillMaxSize()
        ) {
            val (culm) = createRefs()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(575.dp)
                    .constrainAs(culm) {
                        bottom.linkTo(parent.bottom)
                    }
                    .background(
                        color = Color(android.graphics.Color.parseColor("#e0e0e0")),
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .padding(28.dp)
            ) {
                Text(text = "Login", 
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.Black
                )

                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        emailError = !isEmailValid(it)
                    },
                    label = { Text(text = "Email") },
                    shape = RoundedCornerShape(10.dp),
                    isError = emailError && text.isNotBlank(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { senhaFocusRequester.requestFocus() }
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        textColor = Color(android.graphics.Color.parseColor("#5E5E5E")),
                        focusedLabelColor = Color(android.graphics.Color.parseColor("#5E5E5E")),
                        unfocusedLabelColor = Color(android.graphics.Color.parseColor("#5E5E5E")),
                        cursorColor = Color(android.graphics.Color.parseColor("#5E5E5E")),
                        errorBorderColor = Color.Red
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .focusRequester(emailFocusRequester)
                )

                if (emailError && text.isNotBlank()) {
                    Text(
                        text = "Email inválido. Deve conter '@' e terminar com '.com'",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Text(text = "Senha", 
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp),
                    color = Color.Black
                )

                TextField(
                    value = text2,
                    onValueChange = { text2 = it },
                    label = { Text("Senha") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (text.isNotBlank() && text2.isNotBlank() && !emailError) {
                                authViewModel.login(text, text2) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) navigateToHome()
                                }
                            }
                        }
                    ),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(android.graphics.Color.parseColor("#5E5E5E"))
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        textColor = Color(android.graphics.Color.parseColor("#5E5E5E")),
                        focusedLabelColor = Color(android.graphics.Color.parseColor("#5E5E5E")),
                        unfocusedLabelColor = Color(android.graphics.Color.parseColor("#5E5E5E")),
                        cursorColor = Color(android.graphics.Color.parseColor("#5E5E5E")),
                        errorBorderColor = Color.Red
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .focusRequester(senhaFocusRequester)
                )

                val annotatedText = buildAnnotatedString {
                    append("Não tem uma conta? ")

                    pushStringAnnotation(tag = "SIGNUP", annotation = "Register")
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF007BFF), // Azul (cor de link)
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Inscreva-se")
                    }
                    pop()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    contentAlignment = Alignment.Center
                )
                {
                    ClickableText(
                        text = annotatedText,
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth(),
                        style = TextStyle(
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color(android.graphics.Color.parseColor("#5E5E5E"))
                        ),
                        onTextLayout = { textLayoutResult ->
                        },
                        onClick = { offset ->
                            annotatedText.getStringAnnotations("SIGNUP", offset, offset)
                                .firstOrNull()
                                ?.let { navController.navigate("RegisterScreen") }
                        }
                    )
                }

                val forgotPasswordText = buildAnnotatedString {
                    // Define toda a frase como clicável (ou pode marcar apenas parte)
                    pushStringAnnotation(tag = "FORGOT_PW", annotation = "ChangePassword")
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF5E5E5E), // Mesma cor do texto original
                        )
                    ) {
                        append("Esqueceu a sua senha?")
                    }
                    pop()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ClickableText(
                        text = forgotPasswordText,
                        style = TextStyle(
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        onClick = { offset ->
                            forgotPasswordText.getStringAnnotations("FORGOT_PW", offset, offset)
                                .firstOrNull()
                                ?.let { navController.navigate("ChangePassword") }
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .weight(1f)
                            .background(Color(android.graphics.Color.parseColor("#4d4d4d")))
                    )
                    Text(
                        text = "Ou Login com",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                        color = Color(android.graphics.Color.parseColor("#4d4d4d"))
                    )
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .weight(1f)
                            .background(Color(android.graphics.Color.parseColor("#4d4d4d")))
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Primeiro botão
                    Button(
                        onClick = {
                            Toast.makeText(context, "Desenvolvimento em progresso", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Google",
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Continuar com Google",
                                color = Color(android.graphics.Color.parseColor("#2f4f86")),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Desenvolvimento em progresso", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.apple),
                                contentDescription = "Apple",
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Continuar com Apple",
                                color = Color(android.graphics.Color.parseColor("#2f4f86")),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (text.isEmpty() || text2.isEmpty()) {
                            Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                        } else if (emailError) {
                            Toast.makeText(context, "Email inválido. Deve conter '@' e terminar com '.com'", Toast.LENGTH_SHORT).show()
                        } else {
                            authViewModel.login(text, text2) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) navigateToHome()
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Entrar",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}