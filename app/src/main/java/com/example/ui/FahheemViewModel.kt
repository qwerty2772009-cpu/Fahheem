package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiStudyCompanion
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FahheemRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FahheemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FahheemRepository(AppDatabase.getDatabase(application))

    val userState: StateFlow<UserEntity?> = repository.userFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val tasksState: StateFlow<List<TaskEntity>> = repository.allTasksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lessonsState: StateFlow<List<LessonScheduleEntity>> = repository.allLessonsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sessionsState: StateFlow<List<StudySessionEntity>> = repository.allSessionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val achievementsState: StateFlow<List<AchievementEntity>> = repository.allAchievementsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val fadfadaChatsState: StateFlow<List<ChatConversationEntity>> = repository.getChatFlow("FADFADA").stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val prayerTimesState: StateFlow<List<PrayerTimeEntity>> = repository.prayerTimesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentMascotSpeech = MutableStateFlow("دلوقتي وقت المذاكرة. أنا جهزتلك كل حاجة، ابدأ وركز.")
    val currentMascotSpeech: StateFlow<String> = _currentMascotSpeech.asStateFlow()

    private val _isTutorialVisible = MutableStateFlow(false)
    val isTutorialVisible: StateFlow<Boolean> = _isTutorialVisible.asStateFlow()

    private val _currentTutorialStep = MutableStateFlow(0)
    val currentTutorialStep: StateFlow<Int> = _currentTutorialStep.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            updateHomeMascotSpeech()
        }
    }

    fun updateSelectedMascot(mascotId: MascotId) {
        viewModelScope.launch {
            repository.updateSelectedMascot(mascotId)
            updateHomeMascotSpeech()
        }
    }

    fun updateMascotPosition(position: MascotPosition) {
        viewModelScope.launch {
            repository.updateMascotPosition(position)
        }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            repository.updateLanguage(language)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
            updateCommitmentScore()
        }
    }

    fun addNewTask(title: String, subject: String, startTime: String, endTime: String, isHomework: Boolean = false) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-DD", Locale.US).format(Date())
            val newTask = TaskEntity(
                title = title,
                subject = subject,
                startTime = startTime,
                endTime = endTime,
                isHomework = isHomework,
                dateString = today
            )
            repository.addTask(newTask)
            updateHomeMascotSpeech()
        }
    }

    fun logStudySession(subject: String, durationMinutes: Int, understandingLevel: String) {
        viewModelScope.launch {
            repository.logStudySession(subject, durationMinutes, understandingLevel)
            updateCommitmentScore()
            updateHomeMascotSpeech()
        }
    }

    fun handlePostLessonCheck(attended: Boolean, teacherAssignedHomework: Boolean, subjectName: String) {
        viewModelScope.launch {
            if (attended && teacherAssignedHomework) {
                val today = SimpleDateFormat("yyyy-MM-DD", Locale.US).format(Date())
                val homeworkTask = TaskEntity(
                    title = "واجب $subjectName (تلقائي مع الفروض والتزاماتك)",
                    subject = subjectName,
                    startTime = "17:30",
                    endTime = "18:30",
                    isHomework = true,
                    priority = "HIGH",
                    dateString = today
                )
                repository.addTask(homeworkTask)
            }
        }
    }

    private val _isMascotTyping = MutableStateFlow(false)
    val isMascotTyping: StateFlow<Boolean> = _isMascotTyping.asStateFlow()

    fun sendFadfadaMessage(userMessage: String) {
        viewModelScope.launch {
            val user = repository.getUser()
            val mascotId = MascotId.valueOf(user.selectedMascotId)

            val userChat = ChatConversationEntity(
                mascotId = mascotId.name,
                sender = "USER",
                messageText = userMessage,
                category = "FADFADA"
            )
            repository.addChat(userChat)

            _isMascotTyping.value = true

            // Natural typing delay to feel like a real friend typing on WhatsApp
            kotlinx.coroutines.delay((600..1200).random().toLong())

            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val recentChats = fadfadaChatsState.value.takeLast(6).joinToString("; ") { "${it.sender}: ${it.messageText}" }
            val currentTasks = tasksState.value.filter { !it.isCompleted }.take(3).joinToString(", ") { it.title }

            val contextInfo = "Student: ${user.name.ifBlank { "بطل" }}, Grade: ${user.grade}, Commitment: ${user.commitmentPercentage}%, Time: $currentTime, Tasks: [$currentTasks], PreviousHistory: [$recentChats]"

            val aiReply = GeminiStudyCompanion.generateMascotResponse(
                mascotId = mascotId,
                userMessage = userMessage,
                contextInfo = contextInfo,
                isFadfada = true
            )

            val mascotChat = ChatConversationEntity(
                mascotId = mascotId.name,
                sender = "MASCOT",
                messageText = aiReply,
                category = "FADFADA"
            )
            repository.addChat(mascotChat)
            _isMascotTyping.value = false
        }
    }

    fun updateHomeMascotSpeech() {
        viewModelScope.launch {
            val user = repository.getUser()
            val mascotId = MascotId.valueOf(user.selectedMascotId)
            val allTasks = tasksState.value
            val currentTasks = allTasks.filter { !it.isCompleted }
            val completedCount = allTasks.count { it.isCompleted }
            val nextTaskTitle = currentTasks.firstOrNull()?.title

            val advice = GeminiStudyCompanion.getDynamicHomeAdvice(
                mascotId = mascotId,
                taskTitle = nextTaskTitle,
                userName = user.name.ifBlank { "بطل" },
                completedTasksCount = completedCount,
                totalTasksCount = allTasks.size,
                isExamMode = user.isExamMode,
                isRamadanMode = user.isRamadanMode
            )
            _currentMascotSpeech.value = advice
        }
    }

    private fun updateCommitmentScore() {
        viewModelScope.launch {
            val allTasks = tasksState.value
            if (allTasks.isNotEmpty()) {
                val completedCount = allTasks.count { it.isCompleted }
                val newPercentage = ((completedCount.toFloat() / allTasks.size.toFloat()) * 100).toInt()
                repository.updateCommitmentPercentage(newPercentage.coerceIn(20, 100))
            }
        }
    }

    fun startTutorial() {
        _isTutorialVisible.value = true
        _currentTutorialStep.value = 0
    }

    fun nextTutorialStep(totalSteps: Int) {
        if (_currentTutorialStep.value < totalSteps - 1) {
            _currentTutorialStep.value += 1
        } else {
            _isTutorialVisible.value = false
        }
    }

    fun skipTutorial() {
        _isTutorialVisible.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveOnboardingData(
        name: String,
        grade: String,
        subjects: String,
        targetGrade: Int,
        selectedMascotId: MascotId
    ) {
        viewModelScope.launch {
            val currentUser = repository.getUser()
            val updatedUser = currentUser.copy(
                name = name,
                grade = grade,
                subjects = subjects,
                targetGradePercentage = targetGrade,
                selectedMascotId = selectedMascotId.name,
                isOnboardingCompleted = true
            )
            repository.saveUser(updatedUser)
            startTutorial()
        }
    }
}
