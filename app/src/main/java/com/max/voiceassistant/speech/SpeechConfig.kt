package com.max.voiceassistant.speech

/**
 * 百度语音 SDK 配置。
 *
 * 使用前需在百度 AI 开放平台创建应用，将 APP_ID、API_KEY、SECRET_KEY 填入下方；
 * [isConfigValid] 用于判断是否已配置，未配置时 [VoiceAssistantManager] 走 Mock 模式。
 */
object SpeechConfig {
    var mockMode = false

    /**
     * 百度开放平台应用 ID。
     *
     * 公开仓库请勿提交真实凭证；请在本地替换为你自己的值。
     */
    const val APP_ID = "121762125"

    /**
     * 百度开放平台 API Key。
     *
     * 公开仓库请勿提交真实凭证；请在本地替换为你自己的值。
     */
    const val API_KEY = "LfK5oq5s0ctNn9vOyn1mPSBY"

    /**
     * 百度开放平台 Secret Key。
     *
     * 公开仓库请勿提交真实凭证；请在本地替换为你自己的值。
     */
    const val SECRET_KEY = "Z99LhgXTPcOJG1vslD4r5573nT5qZiXT"

    /** ASR 识别参数：采样率、语言、VAD 等。 */
    object ASR {
        const val LANGUAGE = 1537
        const val VAD_ENDPOINT_TIMEOUT = 2000
    }
}