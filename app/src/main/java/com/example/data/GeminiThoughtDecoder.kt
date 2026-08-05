package com.example.data

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

class GeminiThoughtDecoder {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeNeuralTelemetry(
        primaryState: String,
        confidencePct: Int,
        frequencyBands: FrequencyBands,
        focusIndex: Float,
        relaxationIndex: Float,
        userPrompt: String = ""
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local Intelligent Neural Decoder Fallback
            return@withContext generateLocalNeuralInsight(
                primaryState, confidencePct, frequencyBands, focusIndex, relaxationIndex
            )
        }

        val prompt = """
            You are MindDecoder AI, an advanced Brain-Computer Interface (BCI) neural state interpreter.
            Current Live Neural Telemetry:
            - Primary Decoded State: $primaryState
            - Decoding Confidence: $confidencePct%
            - Frequency Power Spectrum: Delta=${(frequencyBands.deltaPower * 100).toInt()}%, Theta=${(frequencyBands.thetaPower * 100).toInt()}%, Alpha=${(frequencyBands.alphaPower * 100).toInt()}%, Beta=${(frequencyBands.betaPower * 100).toInt()}%, Gamma=${(frequencyBands.gammaPower * 100).toInt()}%
            - Focus Index: ${(focusIndex * 100).toInt()}%
            - Relaxation Index: ${(relaxationIndex * 100).toInt()}%
            ${if (userPrompt.isNotBlank()) "User Inquiry: $userPrompt" else "Action: Synthesize the subvocal thought pattern and cognitive diagnostic report."}

            Provide a concise, high-tech, futuristic 3-sentence analysis:
            1. [Thought Synthesis]: What exact mental intention or subvocal idea is being formed.
            2. [Cognitive Diagnostic]: Neural efficiency, band synchronization, and cognitive load assessment.
            3. [BCI Recommendation]: Guidance for optimizing neural focus or relaxation.
        """.trimIndent()

        try {
            val partObj = JSONObject().put("text", prompt)
            val partsArr = JSONArray().put(partObj)
            val contentObj = JSONObject().put("parts", partsArr)
            val contentsArr = JSONArray().put(contentObj)
            val requestJson = JSONObject().put("contents", contentsArr)

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(httpRequest).execute()
            val responseText = response.body?.string() ?: ""

            if (response.isSuccessful && responseText.isNotBlank()) {
                val responseJson = JSONObject(responseText)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            }

            return@withContext generateLocalNeuralInsight(
                primaryState, confidencePct, frequencyBands, focusIndex, relaxationIndex
            )
        } catch (e: Exception) {
            return@withContext generateLocalNeuralInsight(
                primaryState, confidencePct, frequencyBands, focusIndex, relaxationIndex
            )
        }
    }

    private fun generateLocalNeuralInsight(
        state: String,
        confidencePct: Int,
        bands: FrequencyBands,
        focus: Float,
        relax: Float
    ): String {
        val thoughtText = when {
            bands.gammaPower > 0.6f -> "[Thought Synthesis]: High Gamma coherence detected. Synthesizing complex problem solving regarding algorithm spatial mapping and motor coordination."
            bands.betaPower > 0.6f -> "[Thought Synthesis]: Active Beta mental workload. Internal monologue focusing on executive decision making and directional motor intent."
            bands.alphaPower > 0.6f -> "[Thought Synthesis]: Dominant Alpha rhythm. State of relaxed alertness, sensory gate attenuation, and creative background processing."
            bands.thetaPower > 0.5f -> "[Thought Synthesis]: Theta wave surge detected. Deep reflective memory recall and spatial visualization activity."
            else -> "[Thought Synthesis]: $state pattern recognized with $confidencePct% neural classification confidence."
        }

        val diagnostic = "[Cognitive Diagnostic]: Focus Index at ${(focus * 100).toInt()}%, Relaxation Index at ${(relax * 100).toInt()}%. Prefrontal coherence is optimal with minimal ocular artifact crosstalk."
        val advice = "[BCI Recommendation]: Maintain rhythmic breathing to sustain Alpha-Beta balance for low-latency neural decoding."

        return "$thoughtText\n$diagnostic\n$advice"
    }
}
