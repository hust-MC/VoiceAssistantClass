package com.max.voiceassistant.speech

import android.health.connect.datatypes.units.Volume

class SpeechRecognizerManager {
    interface RecognitionListener {
        fun onReady()
        fun onBegin()
        fun onVolumeChanged(volume: Int)
        fun onResult(result: String)
        fun onEnd()
        fun onError(errorCode: Int, errorMessage: String)
    }
}