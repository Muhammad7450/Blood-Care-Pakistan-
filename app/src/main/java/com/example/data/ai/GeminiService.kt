package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateBloodGuidance(userPrompt: String, language: String = "English"): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineResponse(userPrompt, language)
        }

        try {
            val systemInstruction = if (language == "Urdu") {
                "آپ BloodCare Pakistan کے AI طبی معاون ہیں۔ خون کے عطیہ، بلڈ گروپ مطابقت، اور ہنگامی رہنمائی کے بارے میں دوستانہ اور انتہائی درست معلومات دیں۔ جواب اردو میں دیں اور آسان الفاظ استعمال کریں۔"
            } else {
                "You are the AI Medical Assistant for BloodCare Pakistan. Provide friendly, highly accurate, and reassuring guidance about blood donation, blood group compatibility, donation eligibility, and emergency blood sourcing in Pakistan."
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userPrompt))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No response text.")
                    }
                }
            }
            getOfflineResponse(userPrompt, language)
        } catch (e: Exception) {
            getOfflineResponse(userPrompt, language)
        }
    }

    private fun getOfflineResponse(prompt: String, language: String): String {
        val lower = prompt.lowercase()
        return if (language == "Urdu") {
            when {
                lower.contains("o-") || lower.contains("o negative") ->
                    "O Negative (O-) عالمی عطیہ دہندہ (Universal Donor) ہے۔ یہ بلڈ گروپ تمام مریضوں کو دیا جا سکتا ہے۔ ہنگامی صورتحال میں اس کی شدید ضرورت ہوتی ہے۔"
                lower.contains("eligibility") || lower.contains("اہلیت") || lower.contains("عطیہ") ->
                    "خون کا عطیہ دینے کے لیے صحت مند فرد کی عمر 18 سے 60 سال ہونی چاہیے، وزن 50 کلوگرام سے زیادہ ہو، اور پچھلا عطیہ کم از کم 3 ماہ پہلے دیا گیا ہو۔"
                lower.contains("thalassemia") || lower.contains("تھیلیسمیا") ->
                    "تھیلیسیمیا کے مریض بچوں کو ہر 2 سے 4 ہفتے بعد تازہ خون کی ضرورت ہوتی ہے۔ باقاعدہ عطیہ دہندگان ان بچوں کی زندگی بچانے میں اہم کردار ادا کرتے ہیں۔"
                else ->
                    "BloodCare Pakistan AI ہیلپر: خون کے عطیہ سے کسی قسم کی کمزوری نہیں ہوتی، بلکہ نیا خون بنتا ہے۔ ہنگامی بلڈ کی درخواست کے لیے ہوم اسکرین پر 'Need Blood Now' بٹن دبائیں۔"
            }
        } else {
            when {
                lower.contains("o-") || lower.contains("universal") ->
                    "O-Negative (O-) is the Universal Blood Donor! An O- donor can give blood to patients of any blood type (A+, A-, B+, B-, AB+, AB-, O+, O-). Emergency rooms rely heavily on O- blood."
                lower.contains("eligibility") || lower.contains("eligible") || lower.contains("weight") ->
                    "Eligibility Criteria for Blood Donation in Pakistan:\n• Age: 18 – 65 years\n• Weight: At least 50 kg\n• Hemoglobin: Minimum 12.5 g/dL\n• Interval: Minimum 3 months since last donation"
                lower.contains("thalassemia") ->
                    "Thalassemia support: Children suffering from Thalassemia Major require regular blood transfusions every 2-4 weeks. Your recurring donation guarantees life-saving blood for these brave warriors."
                else ->
                    "BloodCare AI Assistant: Donating blood takes less than 30 minutes and can save up to 3 human lives. For instant emergency blood requests, tap the 'Need Blood Now' button on the dashboard."
            }
        }
    }
}
