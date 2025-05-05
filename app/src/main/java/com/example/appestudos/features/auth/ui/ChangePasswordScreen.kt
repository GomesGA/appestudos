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
import androidx.compose.ui.text.input.ImeAction
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
fun ChangePasswordScreen(navController: NavController) {
    val context = LocalContext.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var passwordVisible1 by rememberSaveable { mutableStateOf(false) }
    val viewModel: AuthViewModel = viewModel()
    var emailError by rememberSaveable { mutableStateOf(false) }

    // FocusRequesters para navegação entre campos
    val emailFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val novaSenhaFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val confirmarSenhaFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusManager = LocalFocusManager.current

    fun isEmailValid(email: String): Boolean {
        return email.contains("@") && email.endsWith(".com")
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 50.dp)
            .paint(
                painterResource(id = R.drawable.changepasswordscreen_background),
                contentScale = ContentScale.FillWidth
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
                    .height(530.dp)
                    .constrainAs(culm) {
                        bottom.linkTo(parent.bottom)
                    }
                    .background(
                        color = Color(android.graphics.Color.parseColor("#e0e0e0")),
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .padding(32.dp)
            ) {
                Text(text = "Email", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Black
                )
                var email by rememberSaveable { mutableStateOf("") }

                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = !isEmailValid(it)
                    },
                    label = { Text(text = "Digite seu email") },
                    shape = RoundedCornerShape(10.dp),
                    isError = emailError && email.isNotBlank(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { novaSenhaFocusRequester.requestFocus() }
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
                if (emailError && email.isNotBlank()) {
                    Text(
                        text = "Email inválido. Deve conter '@' e terminar com '.com'",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Text(text = "Nova Senha", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Black
                )
                var novaSenha by rememberSaveable { mutableStateOf("") }

                TextField(
                    value = novaSenha,
                    onValueChange = { novaSenha = it },
                    label = { Text(text = "Digite sua nova senha") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmarSenhaFocusRequester.requestFocus() }
                    ),
                    visualTransformation = if (passwordVisible) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    trailingIcon =  {
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
                        cursorColor = Color(android.graphics.Color.parseColor("#5E5E5E"))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .focusRequester(novaSenhaFocusRequester)
                )

                Text(text = "Confirme a Nova Senha", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Black
                )
                var confirmarSenha by rememberSaveable { mutableStateOf("") }

                TextField(
                    value = confirmarSenha,
                    onValueChange = { confirmarSenha = it },
                    label = { Text(text = "Confirme sua nova senha") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    visualTransformation = if (passwordVisible1) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    trailingIcon =  {
                        IconButton(onClick = { passwordVisible1 = !passwordVisible1 }) {
                            Icon(
                                imageVector = if (passwordVisible1) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible1) "Hide password" else "Show password",
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
                        cursorColor = Color(android.graphics.Color.parseColor("#5E5E5E"))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .focusRequester(confirmarSenhaFocusRequester)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .weight(1f)
                            .background(Color(android.graphics.Color.parseColor("#4d4d4d")))
                    )
                }


                Button(
                    onClick = {
                        if (email.isEmpty() || novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
                            Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                        } else if (emailError) {
                            Toast.makeText(context, "Email inválido. Deve conter '@' e terminar com '.com'", Toast.LENGTH_SHORT).show()
                        } else if (novaSenha != confirmarSenha) {
                            Toast.makeText(context, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.alterarSenha(email, novaSenha) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    navController.navigate("LoginScreen")
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
                ){
                    Text(
                        text = "Alterar Senha",
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
fun ChangePasswordPreview() {
    ChangePasswordScreen(navController = rememberNavController())
}