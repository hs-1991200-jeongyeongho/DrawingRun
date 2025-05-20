package com.example.drawingrun.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drawingrun.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    loading: Boolean = false
) {
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        // 아이디 입력
        CustomTextField(
            value = id,
            onValueChange = { id = it },
            label = "아이디",
            placeholder = "아이디 입력"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 비밀번호 입력
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "비밀번호",
            placeholder = "비밀번호 입력",
            isPassword = true,
            passwordVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLoginClick(id, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF670C))
        ) {
            Text("로그인")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65C00))
        ) {
            Text("회원가입 하기")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onVisibilityToggle: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFF616161), fontWeight = FontWeight.Medium)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 6.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE0E0E0), shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                visualTransformation = when {
                    isPassword && !passwordVisible -> PasswordVisualTransformation()
                    else -> VisualTransformation.None
                },
                keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(placeholder, color = Color(0xFFBDBDBD), fontSize = 14.sp)
                            }
                            innerTextField()
                        }

                        if (isPassword && onVisibilityToggle != null) {
                            IconButton(onClick = onVisibilityToggle) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "비밀번호 보기",
                                    tint = Color(0xFFBDBDBD)
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginClick = { _, _ -> }, onRegisterClick = {})
}
