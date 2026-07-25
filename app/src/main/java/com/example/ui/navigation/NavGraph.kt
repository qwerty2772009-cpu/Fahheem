package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.data.model.AppLanguage
import com.example.data.model.MascotId
import com.example.data.model.MascotPosition
import com.example.ui.FahheemViewModel
import com.example.ui.components.FahheemBottomNavBar
import com.example.ui.components.FrostedBackgroundContainer
import com.example.ui.components.InteractiveTutorialOverlay
import com.example.data.model.Mascots
import com.example.ui.components.TutorialStep
import com.example.ui.screens.achievements.AchievementsScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.fadfada.FadfadaScreen
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.onboarding.WelcomeOnboardingScreen
import com.example.ui.screens.schedule.ScheduleScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.stats.StatsScreen
import com.example.ui.screens.tasks.TasksScreen

@Composable
fun FahheemNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: FahheemViewModel
) {
    val userState by viewModel.userState.collectAsState()
    val tasksState by viewModel.tasksState.collectAsState()
    val lessonsState by viewModel.lessonsState.collectAsState()
    val achievementsState by viewModel.achievementsState.collectAsState()
    val fadfadaChats by viewModel.fadfadaChatsState.collectAsState()
    val sessionsState by viewModel.sessionsState.collectAsState()
    val prayersState by viewModel.prayerTimesState.collectAsState()
    val mascotSpeech by viewModel.currentMascotSpeech.collectAsState()
    val isMascotTyping by viewModel.isMascotTyping.collectAsState()
    val isTutorialVisible by viewModel.isTutorialVisible.collectAsState()
    val currentTutorialStep by viewModel.currentTutorialStep.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val selectedMascot = Mascots.getById(MascotId.valueOf(userState?.selectedMascotId ?: "FAHHEEM"))

    val tutorialSteps = listOf(
        TutorialStep("الشريك الذكي", "بص... من هنا أنا هكلمك وهقولك تعمل إيه بالظبط من غير ما تتعب تفكيرك.", "Mascot"),
        TutorialStep("جلسة المذاكرة", "تقدر تبدأ جلسة المذاكرة بضغطة واحدة مع حارس منع التشتت.", "FocusButton"),
        TutorialStep("فضفضلي", "لو حسيت بتعب أو زهق، اضغط هنا وفضفضلي في أي وقت.", "FadfadaButton")
    )

    val showBottomBar = currentRoute in listOf("home", "schedule", "tasks", "fadfada", "stats", "settings")

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                FahheemBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        FrostedBackgroundContainer(
            modifier = Modifier.padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        val destination = if (userState?.isOnboardingCompleted == true) "home" else "onboarding"
                        navController.navigate(destination) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("auth") {
                AuthScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            composable("onboarding") {
                WelcomeOnboardingScreen(
                    onOnboardingCompleted = { name, grade, subjects, targetGrade, selectedMascot ->
                        viewModel.saveOnboardingData(name, grade, subjects, targetGrade, selectedMascot.id)
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                LaunchedEffect(userState?.selectedMascotId) {
                    viewModel.updateHomeMascotSpeech()
                }
                HomeScreen(
                    user = userState,
                    speechBubbleText = mascotSpeech,
                    tasks = tasksState,
                    lessons = lessonsState,
                    onToggleTask = { viewModel.toggleTaskCompletion(it) },
                    onNavigateToFadfada = { navController.navigate("fadfada") },
                    onNavigateToFocusTimer = { navController.navigate("focus") },
                    onNavigateToSchedule = { navController.navigate("schedule") }
                )
            }

            composable("schedule") {
                ScheduleScreen(
                    lessons = lessonsState,
                    prayers = prayersState,
                    isRamadanMode = userState?.isRamadanMode ?: false,
                    isExamMode = userState?.isExamMode ?: false
                )
            }

            composable("tasks") {
                TasksScreen(
                    tasks = tasksState,
                    onToggleTask = { viewModel.toggleTaskCompletion(it) },
                    onAddNewTask = { title, subject, start, end, isHomework ->
                        viewModel.addNewTask(title, subject, start, end, isHomework)
                    },
                    onPostLessonCheck = { attended, homework, subject ->
                        viewModel.handlePostLessonCheck(attended, homework, subject)
                    }
                )
            }

            composable("fadfada") {
                FadfadaScreen(
                    userMascotId = userState?.selectedMascotId ?: "FAHHEEM",
                    chatMessages = fadfadaChats,
                    isMascotTyping = isMascotTyping,
                    onSendMessage = { viewModel.sendFadfadaMessage(it) }
                )
            }

            composable("focus") {
                FocusScreen(
                    onSessionCompleted = { subject, duration, understanding ->
                        viewModel.logStudySession(subject, duration, understanding)
                    }
                )
            }

            composable("stats") {
                StatsScreen(
                    sessions = sessionsState,
                    commitmentPercentage = userState?.commitmentPercentage ?: 85
                )
            }

            composable("achievements") {
                AchievementsScreen(achievements = achievementsState)
            }

            composable("search") {
                SearchScreen(
                    searchQuery = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    allTasks = tasksState
                )
            }

            composable("settings") {
                SettingsScreen(
                    user = userState,
                    onUpdateMascot = { viewModel.updateSelectedMascot(it) },
                    onUpdateMascotPosition = { viewModel.updateMascotPosition(it) },
                    onUpdateLanguage = { viewModel.updateLanguage(it) },
                    onToggleRamadanMode = { /* Update in state */ },
                    onToggleExamMode = { /* Update in state */ },
                    onLogout = {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Guided Interactive Tutorial Spotlight
        InteractiveTutorialOverlay(
            mascot = selectedMascot,
            isVisible = isTutorialVisible,
            currentStepIndex = currentTutorialStep,
            steps = tutorialSteps,
            onNextStep = { viewModel.nextTutorialStep(tutorialSteps.size) },
            onSkipTutorial = { viewModel.skipTutorial() }
        )
    }
}
}
