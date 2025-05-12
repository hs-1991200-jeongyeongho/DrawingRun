package com.example.drawingrun

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import com.example.drawingrun.ui.theme.DrawingRunTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingRunTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val spoqaFontFamily = FontFamily(
        Font(R.font.spoqa_han_sans_regular, FontWeight.Normal),
        Font(R.font.spoqa_han_sans_bold, FontWeight.Bold),
        Font(R.font.spoqa_han_sans_light, FontWeight.Light),
        Font(R.font.spoqa_han_sans_thin, FontWeight.Thin),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 로고 이미지
        Image(
            painter = painterResource(id = R.drawable.drawing_run_logo),
            contentDescription = "DrawingRun 로고",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .shadow(8.dp, shape = RoundedCornerShape(7.dp))
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 바로 밑에 문구
        Text(
            text = "MAKE YOUR RUN",
            fontSize = 45.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp) // 로고와 살짝 띄우기
        )

        // 경로 선택 버튼
        Button(
            onClick = {
                context.startActivity(Intent(context, DrawingActivity::class.java))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3), // 파란색
                contentColor = Color.White
            )
        ) {
            Text(
                text = "경로 만들기",
                fontSize = 19.sp,
                fontFamily = spoqaFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        // 지도 열기 버튼
        Button(
            onClick = {
                context.startActivity(Intent(context, MapsActivity::class.java))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3), // 초록색
                contentColor = Color.White
            )
        ) {
            Text(
                text = "지도 열기",
                fontSize = 19.sp,
                fontFamily = spoqaFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DrawingRunTheme {
        // 미리보기를 위해 아무것도 호출하지 않거나,
        // MainScreen() 대신 단순 UI만 미리보기용으로 쓸 수 있음
    }
}
