package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Mascot
import com.example.data.model.MascotPosition

data class TutorialStep(
    val titleAr: String,
    val explanationEg: String,
    val highlightTargetName: String
)

@Composable
fun InteractiveTutorialOverlay(
    mascot: Mascot,
    isVisible: Boolean,
    currentStepIndex: Int,
    steps: List<TutorialStep>,
    onNextStep: () -> Unit,
    onSkipTutorial: () -> Unit
) {
    if (!isVisible || steps.isEmpty() || currentStepIndex >= steps.size) return

    val step = steps[currentStepIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onNextStep() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MascotView(
                mascot = mascot,
                speechText = step.explanationEg,
                position = MascotPosition.TOP,
                size = 140.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💡 ${step.titleAr}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "اضغط في أي مكان للشرح التالي (${currentStepIndex + 1}/${steps.size})",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onSkipTutorial) {
                            Text("تخطي الشرح", color = MaterialTheme.colorScheme.outline)
                        }

                        Button(
                            onClick = onNextStep,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (currentStepIndex == steps.size - 1) "فهمت! ابدأ" else "التالي 👈")
                        }
                    }
                }
            }
        }
    }
}
