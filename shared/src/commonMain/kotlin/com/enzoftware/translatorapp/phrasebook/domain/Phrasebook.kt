package com.enzoftware.translatorapp.phrasebook.domain

import com.enzoftware.translatorapp.core.domain.language.Language
import com.enzoftware.translatorapp.core.domain.language.Language.ARABIC
import com.enzoftware.translatorapp.core.domain.language.Language.AZERBAIJANI
import com.enzoftware.translatorapp.core.domain.language.Language.CHINESE
import com.enzoftware.translatorapp.core.domain.language.Language.ENGLISH
import com.enzoftware.translatorapp.core.domain.language.Language.FRENCH
import com.enzoftware.translatorapp.core.domain.language.Language.GERMAN
import com.enzoftware.translatorapp.core.domain.language.Language.ITALIAN
import com.enzoftware.translatorapp.core.domain.language.Language.JAPANESE
import com.enzoftware.translatorapp.core.domain.language.Language.KOREAN
import com.enzoftware.translatorapp.core.domain.language.Language.SPANISH

enum class PhrasebookCategory {
    GREETINGS,
    COURTESY,
    CONVERSATION,
    TRAVEL,
    FOOD,
}

/** One built-in phrase with a translation for every supported [Language]. */
data class PhrasebookEntry(
    /** Stable id used to save the phrase; never change it once shipped. */
    val key: String,
    val category: PhrasebookCategory,
    val translations: Map<Language, String>,
) {
    fun textIn(language: Language): String = translations.getValue(language)
}

/**
 * Offline phrasebook: everyday words and phrases with hand-written translations, so the app is
 * useful (and testable) even when the translation API is unreachable.
 */
object Phrasebook {

    val entries: List<PhrasebookEntry> = listOf(
        entry(
            "hello", PhrasebookCategory.GREETINGS,
            ENGLISH to "Hello", ARABIC to "مرحبًا", AZERBAIJANI to "Salam", CHINESE to "你好",
            FRENCH to "Salut", GERMAN to "Hallo", ITALIAN to "Ciao", JAPANESE to "こんにちは",
            KOREAN to "안녕하세요", SPANISH to "Hola",
        ),
        entry(
            "good_morning", PhrasebookCategory.GREETINGS,
            ENGLISH to "Good morning", ARABIC to "صباح الخير", AZERBAIJANI to "Sabahınız xeyir", CHINESE to "早上好",
            FRENCH to "Bonjour", GERMAN to "Guten Morgen", ITALIAN to "Buongiorno", JAPANESE to "おはようございます",
            KOREAN to "좋은 아침이에요", SPANISH to "Buenos días",
        ),
        entry(
            "good_night", PhrasebookCategory.GREETINGS,
            ENGLISH to "Good night", ARABIC to "تصبح على خير", AZERBAIJANI to "Gecəniz xeyrə qalsın", CHINESE to "晚安",
            FRENCH to "Bonne nuit", GERMAN to "Gute Nacht", ITALIAN to "Buonanotte", JAPANESE to "おやすみなさい",
            KOREAN to "안녕히 주무세요", SPANISH to "Buenas noches",
        ),
        entry(
            "goodbye", PhrasebookCategory.GREETINGS,
            ENGLISH to "Goodbye", ARABIC to "مع السلامة", AZERBAIJANI to "Görüşənədək", CHINESE to "再见",
            FRENCH to "Au revoir", GERMAN to "Auf Wiedersehen", ITALIAN to "Arrivederci", JAPANESE to "さようなら",
            KOREAN to "안녕히 계세요", SPANISH to "Adiós",
        ),
        entry(
            "see_you_tomorrow", PhrasebookCategory.GREETINGS,
            ENGLISH to "See you tomorrow", ARABIC to "أراك غدًا", AZERBAIJANI to "Sabah görüşərik", CHINESE to "明天见",
            FRENCH to "À demain", GERMAN to "Bis morgen", ITALIAN to "A domani", JAPANESE to "また明日",
            KOREAN to "내일 봐요", SPANISH to "Hasta mañana",
        ),
        entry(
            "thank_you", PhrasebookCategory.COURTESY,
            ENGLISH to "Thank you", ARABIC to "شكرًا", AZERBAIJANI to "Təşəkkür edirəm", CHINESE to "谢谢",
            FRENCH to "Merci", GERMAN to "Danke", ITALIAN to "Grazie", JAPANESE to "ありがとうございます",
            KOREAN to "감사합니다", SPANISH to "Gracias",
        ),
        entry(
            "please", PhrasebookCategory.COURTESY,
            ENGLISH to "Please", ARABIC to "من فضلك", AZERBAIJANI to "Zəhmət olmasa", CHINESE to "请",
            FRENCH to "S'il vous plaît", GERMAN to "Bitte", ITALIAN to "Per favore", JAPANESE to "お願いします",
            KOREAN to "부탁합니다", SPANISH to "Por favor",
        ),
        entry(
            "excuse_me", PhrasebookCategory.COURTESY,
            ENGLISH to "Excuse me", ARABIC to "المعذرة", AZERBAIJANI to "Bağışlayın", CHINESE to "打扰一下",
            FRENCH to "Excusez-moi", GERMAN to "Entschuldigung", ITALIAN to "Mi scusi", JAPANESE to "すみません",
            KOREAN to "실례합니다", SPANISH to "Disculpe",
        ),
        entry(
            "sorry", PhrasebookCategory.COURTESY,
            ENGLISH to "Sorry", ARABIC to "آسف", AZERBAIJANI to "Üzr istəyirəm", CHINESE to "对不起",
            FRENCH to "Désolé", GERMAN to "Es tut mir leid", ITALIAN to "Mi dispiace", JAPANESE to "ごめんなさい",
            KOREAN to "죄송합니다", SPANISH to "Lo siento",
        ),
        entry(
            "yes", PhrasebookCategory.COURTESY,
            ENGLISH to "Yes", ARABIC to "نعم", AZERBAIJANI to "Bəli", CHINESE to "是",
            FRENCH to "Oui", GERMAN to "Ja", ITALIAN to "Sì", JAPANESE to "はい",
            KOREAN to "네", SPANISH to "Sí",
        ),
        entry(
            "no", PhrasebookCategory.COURTESY,
            ENGLISH to "No", ARABIC to "لا", AZERBAIJANI to "Xeyr", CHINESE to "不是",
            FRENCH to "Non", GERMAN to "Nein", ITALIAN to "No", JAPANESE to "いいえ",
            KOREAN to "아니요", SPANISH to "No",
        ),
        entry(
            "how_are_you", PhrasebookCategory.CONVERSATION,
            ENGLISH to "How are you?", ARABIC to "كيف حالك؟", AZERBAIJANI to "Necəsiniz?", CHINESE to "你好吗？",
            FRENCH to "Comment allez-vous ?", GERMAN to "Wie geht es Ihnen?", ITALIAN to "Come sta?", JAPANESE to "お元気ですか？",
            KOREAN to "잘 지내세요?", SPANISH to "¿Cómo está?",
        ),
        entry(
            "nice_to_meet_you", PhrasebookCategory.CONVERSATION,
            ENGLISH to "Nice to meet you", ARABIC to "تشرفت بمعرفتك", AZERBAIJANI to "Tanış olmağıma şadam", CHINESE to "很高兴认识你",
            FRENCH to "Enchanté", GERMAN to "Freut mich, Sie kennenzulernen", ITALIAN to "Piacere di conoscerla", JAPANESE to "はじめまして",
            KOREAN to "만나서 반갑습니다", SPANISH to "Encantado de conocerle",
        ),
        entry(
            "i_dont_understand", PhrasebookCategory.CONVERSATION,
            ENGLISH to "I don't understand", ARABIC to "لا أفهم", AZERBAIJANI to "Başa düşmürəm", CHINESE to "我不明白",
            FRENCH to "Je ne comprends pas", GERMAN to "Ich verstehe nicht", ITALIAN to "Non capisco", JAPANESE to "わかりません",
            KOREAN to "이해가 안 돼요", SPANISH to "No entiendo",
        ),
        entry(
            "do_you_speak_english", PhrasebookCategory.CONVERSATION,
            ENGLISH to "Do you speak English?", ARABIC to "هل تتكلم الإنجليزية؟", AZERBAIJANI to "İngiliscə danışırsınız?", CHINESE to "你会说英语吗？",
            FRENCH to "Parlez-vous anglais ?", GERMAN to "Sprechen Sie Englisch?", ITALIAN to "Parla inglese?", JAPANESE to "英語を話せますか？",
            KOREAN to "영어 할 줄 아세요?", SPANISH to "¿Habla inglés?",
        ),
        entry(
            "i_love_you", PhrasebookCategory.CONVERSATION,
            ENGLISH to "I love you", ARABIC to "أحبك", AZERBAIJANI to "Səni sevirəm", CHINESE to "我爱你",
            FRENCH to "Je t'aime", GERMAN to "Ich liebe dich", ITALIAN to "Ti amo", JAPANESE to "愛してる",
            KOREAN to "사랑해요", SPANISH to "Te quiero",
        ),
        entry(
            "good_luck", PhrasebookCategory.CONVERSATION,
            ENGLISH to "Good luck!", ARABIC to "حظًا سعيدًا!", AZERBAIJANI to "Uğurlar!", CHINESE to "祝你好运！",
            FRENCH to "Bonne chance !", GERMAN to "Viel Glück!", ITALIAN to "Buona fortuna!", JAPANESE to "幸運を祈ります！",
            KOREAN to "행운을 빌어요!", SPANISH to "¡Buena suerte!",
        ),
        entry(
            "where_is_the_bathroom", PhrasebookCategory.TRAVEL,
            ENGLISH to "Where is the bathroom?", ARABIC to "أين الحمام؟", AZERBAIJANI to "Tualet haradadır?", CHINESE to "洗手间在哪里？",
            FRENCH to "Où sont les toilettes ?", GERMAN to "Wo ist die Toilette?", ITALIAN to "Dov'è il bagno?", JAPANESE to "トイレはどこですか？",
            KOREAN to "화장실이 어디예요?", SPANISH to "¿Dónde está el baño?",
        ),
        entry(
            "how_much_is_it", PhrasebookCategory.TRAVEL,
            ENGLISH to "How much is it?", ARABIC to "بكم هذا؟", AZERBAIJANI to "Bu neçəyədir?", CHINESE to "多少钱？",
            FRENCH to "Combien ça coûte ?", GERMAN to "Wie viel kostet das?", ITALIAN to "Quanto costa?", JAPANESE to "いくらですか？",
            KOREAN to "얼마예요?", SPANISH to "¿Cuánto cuesta?",
        ),
        entry(
            "help", PhrasebookCategory.TRAVEL,
            ENGLISH to "Help!", ARABIC to "النجدة!", AZERBAIJANI to "Kömək edin!", CHINESE to "救命！",
            FRENCH to "Au secours !", GERMAN to "Hilfe!", ITALIAN to "Aiuto!", JAPANESE to "助けて！",
            KOREAN to "도와주세요!", SPANISH to "¡Ayuda!",
        ),
        entry(
            "water", PhrasebookCategory.FOOD,
            ENGLISH to "Water", ARABIC to "ماء", AZERBAIJANI to "Su", CHINESE to "水",
            FRENCH to "De l'eau", GERMAN to "Wasser", ITALIAN to "Acqua", JAPANESE to "水",
            KOREAN to "물", SPANISH to "Agua",
        ),
        entry(
            "the_bill_please", PhrasebookCategory.FOOD,
            ENGLISH to "The bill, please", ARABIC to "الحساب من فضلك", AZERBAIJANI to "Hesabı gətirin, zəhmət olmasa", CHINESE to "请结账",
            FRENCH to "L'addition, s'il vous plaît", GERMAN to "Die Rechnung, bitte", ITALIAN to "Il conto, per favore", JAPANESE to "お会計をお願いします",
            KOREAN to "계산서 주세요", SPANISH to "La cuenta, por favor",
        ),
        entry(
            "delicious", PhrasebookCategory.FOOD,
            ENGLISH to "Delicious!", ARABIC to "لذيذ جدًا!", AZERBAIJANI to "Çox dadlıdır!", CHINESE to "好吃！",
            FRENCH to "C'est délicieux !", GERMAN to "Lecker!", ITALIAN to "Buonissimo!", JAPANESE to "おいしい！",
            KOREAN to "맛있어요!", SPANISH to "¡Delicioso!",
        ),
        entry(
            "enjoy_your_meal", PhrasebookCategory.FOOD,
            ENGLISH to "Enjoy your meal!", ARABIC to "بالهناء والشفاء!", AZERBAIJANI to "Nuş olsun!", CHINESE to "请慢用！",
            FRENCH to "Bon appétit !", GERMAN to "Guten Appetit!", ITALIAN to "Buon appetito!", JAPANESE to "どうぞ召し上がってください",
            KOREAN to "맛있게 드세요!", SPANISH to "¡Buen provecho!",
        ),
        entry(
            "cheers", PhrasebookCategory.FOOD,
            ENGLISH to "Cheers!", ARABIC to "في صحتك!", AZERBAIJANI to "Sağlığınıza!", CHINESE to "干杯！",
            FRENCH to "Santé !", GERMAN to "Prost!", ITALIAN to "Salute!", JAPANESE to "乾杯！",
            KOREAN to "건배!", SPANISH to "¡Salud!",
        ),
    )

    private val byKey = entries.associateBy { it.key }

    fun byKey(key: String): PhrasebookEntry? = byKey[key]

    private fun entry(key: String, category: PhrasebookCategory, vararg translations: Pair<Language, String>) =
        PhrasebookEntry(key, category, translations.toMap())
}
