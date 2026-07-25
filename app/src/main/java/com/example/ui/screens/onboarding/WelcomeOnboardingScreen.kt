package com.example.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Mascot
import com.example.data.model.Mascots
import com.example.ui.components.MascotSpeechBubble
import com.example.ui.components.MascotView

@Composable
fun WelcomeOnboardingScreen(
    onOnboardingCompleted: (name: String, grade: String, subjects: String, targetGrade: Int, selectedMascot: Mascot) -> Unit
) {
    var selectedMascot by remember { mutableStateOf(Mascots.FAHHEEM_MASCOT) }
    var step by remember { mutableIntStateOf(0) } // 0 = Mascot Showcase, 1 = Name, 2 = Grade, 3 = Subjects & Target

    var userName by remember { mutableStateOf("") }
    var userGrade by remember { mutableStateOf("الثانوية العامة (الصف الثالث)") }
    var userSubjects by remember { mutableStateOf("كيمياء, فيزياء, أحياء, رياضيات") }
    var targetGrade by remember { mutableIntStateOf(95) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (step == 0) {
                // Step 0: Choose Mascot Showcase
                Text(
                    text = "اختار شريك رحلتك. ✨",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "مين هيفكر مكانك وينظم حياتك الأكاديمية؟",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Active mascot spotlight view
                MascotView(
                    mascot = selectedMascot,
                    speechText = selectedMascot.introQuoteEg,
                    size = 150.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mascot selector cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(Mascots.ALL) { mascot ->
                        val isSelected = mascot.id == selectedMascot.id
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .width(130.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedMascot = mascot }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = mascot.imageResId),
                                    contentDescription = mascot.nameAr,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = mascot.nameAr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mascot.personalityTraits.firstOrNull() ?: "",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { step = 1 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "اخترت ${selectedMascot.nameAr}! يلا نبدأ 🎉",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Step 1..3: Chat Bubble Onboarding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "التعارف مع ${selectedMascot.nameAr}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(onClick = {
                        onOnboardingCompleted(
                            userName.ifBlank { "طالب مجتهد" },
                            userGrade,
                            userSubjects,
                            targetGrade,
                            selectedMascot
                        )
                    }) {
                        Text("تخطي وسيب ${selectedMascot.nameAr} يتصرف ⚡")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                MascotView(
                    mascot = selectedMascot,
                    speechText = when (step) {
                        1 -> "أهلاً بيك! أنا ${selectedMascot.nameAr} متحمس جداً أكون معاك. قولي تحب أناديك بإيه؟ 😃"
                        2 -> "تمام يا $userName! أنت في أني مرحلة دراسية أو سنة كام؟"
                        else -> "أهم خطوة! إيه المواد اللي مركز عليها وايه النسبة المئوية اللي بتحلم بيها؟ 🎯"
                    },
                    size = 130.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        when (step) {
                            1 -> {
                                OutlinedTextField(
                                    value = userName,
                                    onValueChange = { userName = it },
                                    label = { Text("اسمك أو اسم الدلع") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { if (userName.isNotBlank()) step = 2 },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("التالي 👈")
                                }
                            }

                            2 -> {
                                val grades = listOf(
                                    "الثانوية العامة (الصف الثالث)",
                                    "الصف الثاني الثانوي",
                                    "الصف الأول الثانوي",
                                    "الجامعة (طالب جامعي)"
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    grades.forEach { gradeOption ->
                                        FilterChip(
                                            selected = userGrade == gradeOption,
                                            onClick = { userGrade = gradeOption },
                                            label = { Text(gradeOption) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { step = 3 },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("التالي 👈")
                                }
                            }

                            3 -> {
                                OutlinedTextField(
                                    value = userSubjects,
                                    onValueChange = { userSubjects = it },
                                    label = { Text("المواد الدراسية (مفصولة بفواصل)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("النسبة المئوية المستهدفة: $targetGrade%", fontWeight = FontWeight.Bold)
                                Slider(
                                    value = targetGrade.toFloat(),
                                    onValueChange = { targetGrade = it.toInt() },
                                    valueRange = 70f..100f
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        onOnboardingCompleted(
                                            userName.ifBlank { "طالب مجتهد" },
                                            userGrade,
                                            userSubjects,
                                            targetGrade,
                                            selectedMascot
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("جاهز! ابدأ رحلتك مع ${selectedMascot.nameAr} 🚀", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
