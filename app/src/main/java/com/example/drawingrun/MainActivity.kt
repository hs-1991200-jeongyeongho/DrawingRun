package com.example.drawingrun

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.drawingrun.ui.theme.DrawingRunTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingRunTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    val spoqaFontFamily = FontFamily(
        Font(R.font.spoqa_han_sans_regular, FontWeight.Normal),
        Font(R.font.spoqa_han_sans_bold, FontWeight.Bold),
        Font(R.font.spoqa_han_sans_light, FontWeight.Light),
        Font(R.font.spoqa_han_sans_thin, FontWeight.Thin),
    )

    // 로고 애니메이션
    val logoScale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutBack)
        )
    }

    // 설명 텍스트 fade-in
    val textAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(310)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFDFDFD), Color(0xFFFAFAFA))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        // 로고 + 배경 원형
        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer {
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                }
                .background(Color(0xFFECEFF1).copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "DrawingRun 로고",
                modifier = Modifier.size(130.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 설명 텍스트
        Text(
            text = "그림을 그리고\n러닝을 시작하세요",
            fontSize = 16.sp,
            color = Color(0xFF616161),
            fontFamily = spoqaFontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(textAlpha.value)
        )

        Spacer(modifier = Modifier.weight(1f))

        // 시작 버튼
        Button(
            onClick = {
                context.startActivity(Intent(context, DrawingActivity::class.java))
            },
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6D00),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(6.dp),
            modifier = Modifier
                .width(200.dp)
                .height(56.dp)
        ) {
            Text(
                text = "시작",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = spoqaFontFamily,
                modifier = Modifier.padding(top = 1.dp) // 시각 중심 보정
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 슬로건 텍스트 (진하게 수정)
        Text(
            text = "지금, 당신의 러닝을 그려보세요",
            fontSize = 13.sp,
            color = Color(0xFF757575),
            fontFamily = spoqaFontFamily
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 하단 버전 정보 (구분자 추가)
        Text(
            text = "v1.0.1 • DrawingRun",
            fontSize = 12.sp,
            color = Color.LightGray
        )
    }
}

@Preview(showBackground = true, name = "Main 화면 미리보기")
@Composable
fun MainScreenPreview() {
    DrawingRunTheme {
        MainScreen()
    }
}
