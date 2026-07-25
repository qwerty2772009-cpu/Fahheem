package com.example.data.model

import com.example.R

enum class MascotId {
    FAHHEEM,
    LOLY,
    ROCKY,
    BEAR,
    BATTOOT
}

enum class MascotPosition {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM
}

enum class MascotEmotion {
    IDLE,
    HAPPY,
    THINKING,
    EXCITED,
    SAD,
    SLEEPING,
    TALKING,
    POINTING
}

data class Mascot(
    val id: MascotId,
    val nameAr: String,
    val nameEn: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val introQuoteEg: String,
    val introQuoteAr: String,
    val introQuoteEn: String,
    val imageResId: Int,
    val primaryColorHex: String,
    val accentColorHex: String,
    val personalityTraits: List<String>
)

object Mascots {
    val FAHHEEM_MASCOT = Mascot(
        id = MascotId.FAHHEEM,
        nameAr = "فهيم",
        nameEn = "FAHHEEM",
        titleAr = "المفكر والمخطط الذكي",
        titleEn = "The Wise Planner",
        descriptionAr = "عقل مفكر ومنظم، بيحسب كل خطوة في يومك وبيفكر بدالك من غير توتر.",
        descriptionEn = "Wise, calm, and intelligent study partner who plans ahead.",
        introQuoteEg = "أنا هفكر مكانك وأنظم يومك.",
        introQuoteAr = "أنا سأفكر بدلاً منك وأُنظم يومك.",
        introQuoteEn = "I'll do the thinking and organize your day.",
        imageResId = R.drawable.img_mascot_fahheem_1784979930108,
        primaryColorHex = "#2563EB",
        accentColorHex = "#60A5FA",
        personalityTraits = listOf("منظم جداً", "بيفكر قبلك", "هادئ وحكيم")
    )

    val LOLY_MASCOT = Mascot(
        id = MascotId.LOLY,
        nameAr = "لولي",
        nameEn = "LOLY",
        titleAr = "صديقتك اللطيفة والمشجعة",
        titleEn = "Your Sweet Bestie",
        descriptionAr = "قطة كيوت وجميلة، بتعامل كأقرب صديقة بتشجعك وتخفف عنك المذاكرة.",
        descriptionEn = "Cute, playful cat who treats you like a best friend.",
        introQuoteEg = "تعالى نذاكر ونخلص الهم ده سوا. 🥹🤍",
        introQuoteAr = "تعال لندرس وننتهي من هذا العبء معاً. 🥹🤍",
        introQuoteEn = "Let's study together and get this done! 🥹🤍",
        imageResId = R.drawable.img_mascot_loly_1784979946216,
        primaryColorHex = "#EC4899",
        accentColorHex = "#F472B6",
        personalityTraits = listOf("صديقة مقربة", "بتشجع بالحب", "كيوت ومرحة")
    )

    val ROCKY_MASCOT = Mascot(
        id = MascotId.ROCKY,
        nameAr = "روكي",
        nameEn = "ROCKY",
        titleAr = "الثعلب الذكي والساخر",
        titleEn = "The Clever Fox",
        descriptionAr = "ثعلب ذكي وساخر، بيقفش أعذارك وينكشك بطريقة تحمسك وتخليك تتحدى نفسك.",
        descriptionEn = "Smart, sarcastic fox who challenges you constructively.",
        introQuoteEg = "أنا مش هسيبك تكسل.. هنذاكر ولا مستني المنهج يذاكر نفسه؟ 😏",
        introQuoteAr = "لن أتركك تتكاسل.. هل سندرس أم تنتظر المنهج ليدرس نفسه؟ 😏",
        introQuoteEn = "I won't let you slack off. Shall we study or wait for the syllabus to read itself? 😏",
        imageResId = R.drawable.img_mascot_rocky_1784979959496,
        primaryColorHex = "#EA580C",
        accentColorHex = "#FB923C",
        personalityTraits = listOf("ساخر ومحفز", "بيقفش الأعذار", "تحدي وحماس")
    )

    val BEAR_MASCOT = Mascot(
        id = MascotId.BEAR,
        nameAr = "دبدوب",
        nameEn = "DABDOUB",
        titleAr = "الدب الزهقان.. بس خايف عليك",
        titleEn = "Grumpy Bear",
        descriptionAr = "دب كسلان وزهقان، ردوده قصيرة ومباشرة، بس جواه حنية وخوف حقيقي عليك.",
        descriptionEn = "Bored, short-spoken bear with dry humor who genuinely cares.",
        introQuoteEg = "خلص اللي وراك... عشان نخلص ونستريح.",
        introQuoteAr = "أنهِ ما عليك... لننتهي ونستريح.",
        introQuoteEn = "Finish what you have... so we can wrap this up.",
        imageResId = R.drawable.img_mascot_bear_1784979973579,
        primaryColorHex = "#65A30D",
        accentColorHex = "#A3E635",
        personalityTraits = listOf("زهقان ومباشر", "ردود قصيرة", "خايف على مصلحتك")
    )

    val BATTOOT_MASCOT = Mascot(
        id = MascotId.BATTOOT,
        nameAr = "بطوط",
        nameEn = "BATTOOT",
        titleAr = "البطريق الخجول واللطيف",
        titleEn = "Gentle Battoot",
        descriptionAr = "بطريق رقيق وخجول، بيطمنك واحدة واحدة وبيخليك تحس بالأمان والهدوء.",
        descriptionEn = "Shy, gentle penguin who reassures you step by step.",
        introQuoteEg = "متقلقش... هنمشي واحدة واحدة والخير جاي. 🤍",
        introQuoteAr = "لا تقلق... سنمشي خطوة بخطوة. 🤍",
        introQuoteEn = "Don't worry... we'll go step by step. 🤍",
        imageResId = R.drawable.img_mascot_battoot_1784979987307,
        primaryColorHex = "#0EA5E9",
        accentColorHex = "#38BDF8",
        personalityTraits = listOf("خجول ورقيق", "بيطمنك دائماً", "صبر وهدوء")
    )

    val ALL = listOf(FAHHEEM_MASCOT, LOLY_MASCOT, ROCKY_MASCOT, BEAR_MASCOT, BATTOOT_MASCOT)

    fun getById(id: MascotId): Mascot = ALL.firstOrNull { it.id == id } ?: FAHHEEM_MASCOT
}
