package com.example.appestudos


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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.appestudos.ui.theme.NationalParkFamily
import androidx.compose.material.IconButton as IconButton


@Composable
fun cadastro(navController: NavController) {
    val context = LocalContext.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var passwordVisible1 by rememberSaveable { mutableStateOf(false) }
    val dbHelper = remember { UserDatabaseHelper(context) }

    Column(
        Modifier
            .fillMaxSize()
            .paint(
                painterResource(id = R.drawable.background_page),
                contentScale = ContentScale.FillWidth
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConstraintLayout(
            Modifier
                .fillMaxSize()
        ) {
            val (topText, culm) = createRefs()

            Text(
                text = "Criar sua Conta",
                color = Color.White,
                modifier = Modifier
                    .padding(top = 16.dp, start = 32.dp)
                    .constrainAs(topText) {
                        linkTo(parent.top, culm.top, bias = 0.6f)
                        linkTo(parent.start,parent.end, bias = 0f)
                    },
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NationalParkFamily
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .constrainAs(culm) {
                        bottom.linkTo(parent.bottom)
                    }
                    .background(
                        color = Color(android.graphics.Color.parseColor("#e0e0e0")),
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .padding(32.dp)
            ) {
                Text(text = "Login", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Black
                )
                var text by rememberSaveable { mutableStateOf("") }

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(text = "Escreva seu Login") },
                    shape = RoundedCornerShape(10.dp),
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
                )

                Text(text = "Senha", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Black
                )
                var text2 by rememberSaveable { mutableStateOf("") }
                
                TextField(
                    value = text2,
                    onValueChange = { text2 = it },
                    label = { Text(text = "Escreva a sua senha") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                )

                Text(text = "Confirme a Sua Senha", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.Black
                )
                var text3 by rememberSaveable { mutableStateOf("") }

                TextField(
                    value = text3,
                    onValueChange = { text3 = it },
                    label = { Text(text = "Escreva a sua senha") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                        if (text.isEmpty() || text2.isEmpty() || text3.isEmpty()) {
                            Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                        } else if (text2 != text3) {
                            Toast.makeText(context, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                        } else if (dbHelper.checkUsernameExists(text)) {
                            Toast.makeText(context, "Nome de usuário já existe", Toast.LENGTH_SHORT).show()
                        } else {
                            if (dbHelper.addUser(text, text2)) {
                                Toast.makeText(context, "Cadastro Criado com sucesso", Toast.LENGTH_SHORT).show()
                                navController.navigate("login")
                            } else {
                                Toast.makeText(context, "Erro ao criar cadastro", Toast.LENGTH_SHORT).show()
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
fun CadastroPreview() {
    cadastro(navController = rememberNavController())
}