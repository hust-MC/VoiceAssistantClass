package com.max.voiceassistant.speech

object SpeechConfig {
    var mockMode = false

    // 大家自行填充
    const val APP_ID = "121762125"
    const val API_KEY = "LfK5oq5s0ctNn9vOyn1mPSBY"
    const val SECRET_KEY = "Z99LhgXTPcOJG1vslD4r5573nT5qZiXT"

    object ASR {
        const val LANGUAGE = 1537
        const val VAD_ENDPOINT_TIMEOUT = 2000
    }

    object TTS {
        const val SPEAKER = 0
        const val SPEED = 5
        const val PITCH = 5
        const val VOLUME = 5
    }
}