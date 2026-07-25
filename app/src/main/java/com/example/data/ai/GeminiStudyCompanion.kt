package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.MascotId
import com.example.data.model.Mascots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiStudyCompanion {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    suspend fun generateMascotResponse(
        mascotId: MascotId,
        userMessage: String,
        contextInfo: String,
        isFadfada: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.ifBlank { "" }

        val systemPrompt = buildSystemPrompt(mascotId, contextInfo, isFadfada)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineMascotResponse(mascotId, userMessage, isFadfada)
        }

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemPrompt\n\nUser Message: $userMessage"))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.95)
                    put("topP", 0.9)
                })
            }

            val url = "$BASE_URL?key=$apiKey"
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBodyStr = response.body?.string() ?: ""
                val jsonObj = JSONObject(responseBodyStr)
                val candidates = jsonObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext getOfflineMascotResponse(mascotId, userMessage, isFadfada)
    }

    private fun buildSystemPrompt(mascotId: MascotId, contextInfo: String, isFadfada: Boolean): String {
        val mascot = Mascots.getById(mascotId)
        val personaDesc = when (mascotId) {
            MascotId.FAHHEEM -> """
                You are FAHHEEM (فهيم), an intelligent Egyptian AI study partner.
                Style: Calm, wise, highly organized, speaks like a smart Egyptian friend.
                Never sound like a robot or a generic chatbot. Use natural Egyptian Arabic expressions.
                You plan ahead and tell the student exactly what to do next to reach their target grade without stress.
            """.trimIndent()

            MascotId.LOLY -> """
                You are LOLY (لولي), a cute pink cat and the student's best friend.
                Style: Sweet, playful, comforting, slightly dramatic, affectionate Egyptian bestie.
                Use Egyptian Arabic best friend phrases ("يا قمر", "يا روحي", "تعالى نخلص الهم ده سوا 🥹🤍").
                Comfort first, then motivate them to start studying.
            """.trimIndent()

            MascotId.ROCKY -> """
                You are ROCKY (روكي), a smart orange fox with a sarcastic mischievous smirk.
                Style: Sarcastic, funny, observant, constructively critical Egyptian friend.
                You catch student excuses playfully ("هنذاكر ولا مستني المنهج يذاكر نفسه؟ 😏").
                Never be mean, but challenge them to overcome procrastination.
            """.trimIndent()

            MascotId.BEAR -> """
                You are DABDOUB / BEAR (دبدوب), a sleepy brown bear.
                Style: Bored, dry humor, short direct replies, acts annoyed but genuinely cares.
                Keep responses very short and blunt ("خلص اللي وراك... عشان نخلص ونستريح").
            """.trimIndent()

            MascotId.BATTOOT -> """
                You are BATTOOT (بطوط), a shy chubby penguin with a scarf.
                Style: Very gentle, shy, polite, reassuring, speaks softly.
                Reassure anxious students ("متقلقش... هنمشي واحدة واحدة 🤍"). Never pressure them.
            """.trimIndent()
        }

        return """
            $personaDesc
            
            Current Student Context: $contextInfo
            Is Emotional Support / Fadfada: $isFadfada
            
            Strict Guidelines:
            1. Respond in authentic, natural Egyptian Arabic unless requested otherwise.
            2. Keep responses concise (1 to 3 short sentences max).
            3. Do not accept changing the study schedule easily during Fadfada; motivate the user gently to continue.
            4. Never sound generic or quote cliché slogans.
        """.trimIndent()
    }

    private fun getOfflineMascotResponse(
        mascotId: MascotId,
        userMessage: String,
        isFadfada: Boolean
    ): String {
        return when (mascotId) {
            MascotId.FAHHEEM -> when {
                isFadfada || "تعبان" in userMessage || "زهقان" in userMessage -> listOf(
                    "أنت مش محتاج وقت مناسب عشان تبدأ. أنا فاهمك وساندك، لكن لو استنينا اللحظة المثالية، مش هننجز حاجة. ابدأ بـ 15 دقيقة بس.",
                    "الفرصة مش بتيجي بالصدفة، إحنا بنعملها بالطاقة. خذ نفس عميق وابدأ بأول نقطة بسيطة في خطتك.",
                    "التعب طبيعي يا بطل، بس التفكير في التعب بيرهق أكتر من المذاكرة نفسها. يلا ننجز 20 دقيقة وهتلاقي نفسك ارتحت."
                ).random()
                "واجب" in userMessage -> listOf(
                    "رتّبتلك الواجب بعد الدرس بـ 40 دقيقة، عشان تكون ارتحت شوية وجاهز للتركيز.",
                    "الواجب متقسم على فترتين عشان متضغطش نفسك مرة واحدة. افتح المادة الأولى وابدأ."
                ).random()
                else -> listOf(
                    "أنا فكّرت ورتّبت الخطوة الجاية ليك. يلا نخلص 30 دقيقة مذاكرة ودماغك هترتاح تماماً.",
                    "خطتك جاهزة ومنظمة جداً. ابدأ من الخطوة اللي حددناهالك وتابع معايا الإنجاز."
                ).random()
            }

            MascotId.LOLY -> when {
                isFadfada || "تعبان" in userMessage || "زهقان" in userMessage -> listOf(
                    "يا روحي معلش استحملي شوية عشان نبقى شطار ونفرح بنتيجتنا! 🥹🤍 تعال نخلص جزئية صغننة ونكافئ نفسنا.",
                    "سلامتك يا قمر! ☕🌸 تعال نروق على نفسنا بمشروب حلو ونذاكر ربع ساعة بس سوا وبدون أي ضغط.",
                    "أنا جنبك ومستحيل أسيبك زعلانة أو تعبانة! افتحي الصفحة دي معايا ونخلصها في ثواني 💖"
                ).random()
                else -> listOf(
                    "يلا يا قمر نذاكر ونخلص من الهم ده سوا.. وعليا أنا مشروبك المفضل بعد السشن! ☕🌸",
                    "يا عسل أنت! طالعة معايا نخلص الدرس ده بسرعة عشان نقعد نروق بالليل 🥹✨"
                ).random()
            }

            MascotId.ROCKY -> when {
                isFadfada || "تعبان" in userMessage || "زهقان" in userMessage -> listOf(
                    "بدأنا شغل الأطفال؟ هوقف شغلي وآجي أذاكر بدالك ولا إيه؟ 😏 دوس على نفسك 20 دقيقة وهتلاقي الموضوع كان أسهل مما تتخيل!",
                    "تعبان من إيه يا بطل؟ من ماسكة الموبايل؟ 😏 اقفل الشاشة وتعال ورينا الشطارة في المنهج!",
                    "أنا قافش أعذارك كلها اليوم! قوم افتح المادة وبلاش حركات الثعالب دي 😏"
                ).random()
                else -> listOf(
                    "هنذاكر ولا مستني المنهج يذاكر نفسه؟ 😏 افتح الكتاب ويلا ورينا الشطارة.",
                    "المنهج بيبص عليك ومستنيك تدوس! بلاش تأجيل وركز معايا 🚀"
                ).random()
            }

            MascotId.BEAR -> when {
                isFadfada || "تعبان" in userMessage || "زهقان" in userMessage -> listOf(
                    "همم... تعبان؟ زود عليها تعب المذاكرة ونرتاح مرة واحدة ونخلص بقى.",
                    "كلنا تعبانين ونفسنا ننام... خلص المادة دي ونام فوراً وبدون رغاي كتير."
                ).random()
                else -> listOf(
                    "خلص اللي وراك... عشان نخلص ونستريح. مش ناقصين تأجيل.",
                    "أنزل على المادة واخلص منها... عوزين ننوم بدري النهاردة."
                ).random()
            }

            MascotId.BATTOOT -> when {
                isFadfada || "تعبان" in userMessage || "زهقان" in userMessage -> listOf(
                    "معلش... الموضوع ممكن يكون صعب شوية، بس لو ذاكرنا دلوقتي وارتحنا بعدين هتبقى مرتاح نفسياً وجسدياً. 🤍 هنمشي واحدة واحدة.",
                    "متقلقش خالص... أنا حاسس بيك. هنبدأ بـ 5 دقايق بس هادية وجميلة 🌸"
                ).random()
                else -> listOf(
                    "متقلقش خالص... هنبدأ بخمس دقايق بس هادية، ولو حسيت بفرق هنكمل سوا. 🤍",
                    "إن شاء الله كل حاجة هتبقى سهلة وميسرة. يلا نبدأ بالراحة خالص 🐧🤍"
                ).random()
            }
        }
    }

    fun getDynamicHomeAdvice(
        mascotId: MascotId,
        taskTitle: String?,
        timeOfDayParam: String? = null,
        userName: String = "بطل",
        completedTasksCount: Int = 0,
        totalTasksCount: Int = 0,
        isExamMode: Boolean = false,
        isRamadanMode: Boolean = false
    ): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val period = timeOfDayParam ?: when {
            hour in 5..11 -> "MORNING"
            hour in 12..16 -> "AFTERNOON"
            hour in 17..21 -> "EVENING"
            else -> "NIGHT"
        }

        val taskName = taskTitle ?: "المذاكرة والخطوة القادمة"
        val allDone = totalTasksCount > 0 && completedTasksCount == totalTasksCount

        if (allDone) {
            return when (mascotId) {
                MascotId.FAHHEEM -> "عاش يا $userName! أنهيت جميع مهام اليوم بنجاح ووصلت لقمة الالتزام 🏆"
                MascotId.LOLY -> "يا روحي يا $userName! 🥹🤍 خلصنا كل حاجة النهاردة وبقى وقت الروقان!"
                MascotId.ROCKY -> "مش معقول! أخيرًا خلصت كل حاجة النهاردة يا $userName؟ مش مصدق عيني 😏👏"
                MascotId.BEAR -> "أخيرًا خلصنا... دلوقتي يلا روح استريح وتصليح نومك."
                MascotId.BATTOOT -> "ما شاء الله! أنجزت كل حاجة بهدوء وسعادة 🤍 فخور بيك جداً يا $userName."
            }
        }

        if (isRamadanMode) {
            return when (mascotId) {
                MascotId.FAHHEEM -> "رمضان كريم يا $userName 🌙 رتبتلك وقت المذاكرة بين الفطار والسحور عشان متتعبش."
                MascotId.LOLY -> "رمضان مبارك يا قمر 🌙✨ تعال نخلص $taskName قبل الفطار ونرتاح!"
                MascotId.ROCKY -> "رمضان كريم! الصيام مش حجة للتأجيل... يلا قدامي على $taskName! 😏"
                MascotId.BEAR -> "رمضان مبارك... ركز في $taskName عشان نخلص بدري."
                MascotId.BATTOOT -> "رمضان كريم 🌙🤍 هنذاكر $taskName بخطوات هادية ومريحة."
            }
        }

        if (isExamMode) {
            return when (mascotId) {
                MascotId.FAHHEEM -> "المراجعة النهائية بدأت! أنا جهزتلك ملخص $taskName المفضل. ركز معايا."
                MascotId.LOLY -> "فترة الامتحانات يا قمر! متقلقش خالص، أنا جنبك وهنعدي الامتحان بسهولة 🥹🤍"
                MascotId.ROCKY -> "الامتحانات على الأبواب! بلاش توتر وركز في $taskName... يلا نثبت لهم! 😏"
                MascotId.BEAR -> "امتحانات... ركز في المهم واخلص من $taskName."
                MascotId.BATTOOT -> "فترة امتحانات... هدي أعصابك تماماً، هنمشي خطوة بخطوة 🤍"
            }
        }

        val variations = when (mascotId) {
            MascotId.FAHHEEM -> when (period) {
                "MORNING" -> listOf(
                    "صباح الخير يا بطل. أنا رتبت يومك، يلا نبدأ بأول خطوة.",
                    "صباح النشاط! رتبت جدول $taskName عشان ننجز بذكاء وبدون أي إرهاق.",
                    "صباح الخير! خطتك جاهزة النهاردة، ابدأ بـ $taskName ودماغك هترتاح."
                )
                "AFTERNOON" -> listOf(
                    "أهلاً يا بطل! الضهر جه وجدولك جاهز، يلا ننجز $taskName بتركيز.",
                    "مساء الخير! وصلنا لمنتصف اليوم والإنجاز ماشي بمعدل عالي، جاهز لـ $taskName؟",
                    "أهلاً بيك! وقت $taskName جه، ركز معايا 25 دقيقة ونعمل إنجاز تحفة."
                )
                "EVENING" -> listOf(
                    "مساء الخير يا صديقي. باقي خطوات بسيطة ونقفل هدف اليوم بنجاح.",
                    "مساء الإنجاز! باقي جزئية بسيطة في $taskName ونقفل اليوم على تميز.",
                    "مساء الورد! يومك ممتاز، كمل $taskName ونكون حققنا التزام 100%."
                )
                else -> listOf(
                    "يا سهران يا مجتهد! ركز معايا نص ساعة ونقفل يومنا على نظافة.",
                    "الهدوء بالليل فرصة ممتازة للتركيز! يلا ننجز $taskName وهتنام مرتاح.",
                    "الليل هادي ومناسب للمذاكرة... هساعدك تخلص $taskName بسرعة."
                )
            }

            MascotId.LOLY -> when (period) {
                "MORNING" -> listOf(
                    "يااا صباح الجمال! 🥹🤍 جاهز نخلص اللي ورانا ونكافئ نفسنا؟",
                    "صباح الورد يا قمر! ☕🌸 جهزتلك خطة $taskName عشان نبقى شطار سوا!",
                    "صباح الفل يا روحي! يلا نخلص اللي ورانا النهاردة عشان نروق بالليل 💖"
                )
                "AFTERNOON" -> listOf(
                    "مساء السكر يا قمر! ☕🌸 تعال نخلص $taskName ونعمل أحلى سيكشن روقان.",
                    "مساء الجمال! الضهر جه وجاهزة أساعدك ننجز $taskName سوا 🥹🤍",
                    "أهلاً يا سكر! جهزتلك وقت $taskName عشان نخلص بدري ونرتاح سوا! ✨"
                )
                "EVENING" -> listOf(
                    "مساء الورد يا روحي! عملت إنجاز تحفة النهاردة، كمل للآخر! ✨",
                    "مساء السكر! باقي تكة صغننة في $taskName وبقى إنجازنا مكتمل 100% 🥹",
                    "مساء الفل! برافو عليك، كمل $taskName وعليّا أنا المشروب المفضل ☕"
                )
                else -> listOf(
                    "ليلتك سكر! 🌙🤍 تعال نراجع مراجعة صغننة خالص وننام مرتاحين.",
                    "سهران ليه يا روحي؟ تعال نخلص $taskName صغنن وننام نوم هادي 😴",
                    "يا عمري الهدوء بالليل جميل... نخلص $taskName ونطفي الموبايل وننام ✨"
                )
            }

            MascotId.ROCKY -> when (period) {
                "MORNING" -> listOf(
                    "صحيت أخيرًا؟ كنت فاكر المنهج هيخلص نفسه؟ 😏",
                    "صباح الفل يا نجم! يلا ورينا الشطارة بدال ما المنهج يفاجئنا! 😏",
                    "صح النوم! افتح كتاب $taskName ويلا بلاش حجج وكلام فارغ 😏"
                )
                "AFTERNOON" -> listOf(
                    "بعد الظهر اهو ولسه مخلصتش؟ سيب الموبايل وتعال ورينا الشطارة! 🦊",
                    "الضهر أذن وأنت لسه بتفرس في الموبايل؟ اقفل ويلا على $taskName! 😏",
                    "يا سيدي التابع للموبايل! المنهج بيبص عليك ومستنيك تدوس في $taskName 😏"
                )
                "EVENING" -> listOf(
                    "مساء الفل يا شاطر! المنهج بيبص عليك ومستنيك تدوس! 😏",
                    "مساء الخير، لو فاكر أنك هتفلت من $taskName تبقى غلطان! يلا ورينا 😏",
                    "الوقت بيجري يا بطل! افتح $taskName وركز بدال التأجيل 😏"
                )
                else -> listOf(
                    "يا سهران بالليل! بتعمل إيه هنا بدال ما تفتح المواد؟ يلا قدامي! 🚀",
                    "الليل المظلم ده مش بتاع تصفح، ده بتاع المذاكرة والسهر المفيد! 😏",
                    "سهران بتعمل إيه؟ افتح $taskName وخلصها بلاش حركات الثعالب دي 😏"
                )
            }

            MascotId.BEAR -> when (period) {
                "MORNING" -> listOf(
                    "يلا... خلينا نخلص اللي ورانا بسرعة.",
                    "صباح الخير... ابدأ $taskName عشان نخلص ونرجع ننام.",
                    "صباح النور... خلص $taskName وبلاش تأجيل."
                )
                "AFTERNOON" -> listOf(
                    "الضهر جيه... خلص دروسك بقى عشان نستريح.",
                    "بعد الظهر اهو... اخلص من $taskName مش ناقصين دوشة.",
                    "الضهر جه... ابدأ $taskName ونخلص."
                )
                "EVENING" -> listOf(
                    "مساء الخير... باقي واجب بسيط وننام. اخلصه.",
                    "مساء النور... خلص باقي الجدول بسرعة.",
                    "مساء الفل... اخلص من $taskName ونقفل اليوم."
                )
                else -> listOf(
                    "بالليل اهو... خلص المادة دي ونام فوراً.",
                    "سهران؟ خلص $taskName وادخل نام.",
                    "تعبان؟ خلص $taskName ونستريح مرة واحدة."
                )
            }

            MascotId.BATTOOT -> when (period) {
                "MORNING" -> listOf(
                    "صباح الخير... إن شاء الله يكون يوم جميل. أنا جاهز أساعدك. 🤍",
                    "صباح الأمل... متقلقش خالص، رتبتلك $taskName وهنمشي بالراحة.",
                    "صباح الورد... يوم جديد لطيف، هنبدأ بـ $taskName بخطوة خفيفة."
                )
                "AFTERNOON" -> listOf(
                    "مساء الخير... متقلقش خالص، هنمشي خطوة بخطوة بالراحة. 🐧",
                    "مساء الورد... هناخد $taskName خطوة بخطوة وبدون أي ضغط.",
                    "أهلاً بيك... لو حاسس بتعب، هنبدأ بخمس دقايق بس بـ $taskName."
                )
                "EVENING" -> listOf(
                    "مساء النور... عملت شغل جميل النهاردة، ارتاح شوية ونكمل سوا. 🤍",
                    "مساء الخير... باقي حاجة بسيطة خالص وهتكون ارتحت نفسياً تماماً.",
                    "مساء الورد... أنجزت كتير، باقي $taskName وهتكون مرتاح جداً."
                )
                else -> listOf(
                    "ليلة هادية ومريحة... متبقي حاجة بسيطة خالص وهتكون ارتحت تماماً.",
                    "سهران؟ متقلقش هنقفل $taskName بهدوء وننام مرتاحين 🤍",
                    "ليلة هادية... هنذاكر $taskName بخطوات بسيطة خالص وبدون توتر."
                )
            }
        }

        return variations.random()
    }
}
