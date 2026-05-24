package com.max.voiceassistant.speech

import android.util.Log

class VoiceAssistantManager {

    private var mockManager: MockSpeechManager? = null
    private var recognitionCallback: RecognitionCallback? = null
    private var isInitialized = false


    fun setRecognitionCallback(callback: RecognitionCallback) {
        recognitionCallback = callback
    }

    private fun initMockMode(): Boolean {
        Log.d(TAG, "初始化Mock模式")

        mockManager = MockSpeechManager()
        mockManager?.setRecognitionListener(object : SpeechRecognizerManager.RecognitionListener {
            override fun onReady() {
                recognitionCallback?.onReady()
            }

            override fun onBegin() {
                recognitionCallback?.onBegin()
            }

            override fun onVolumeChanged(volume: Int) {
                recognitionCallback?.onVolumeChanged(volume)
            }

            override fun onResult(result: String) {
                recognitionCallback?.onResult(result)
            }

            override fun onEnd() {
                recognitionCallback?.onEnd()
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                recognitionCallback?.onError(errorCode, errorMessage)
            }
        })

        isInitialized = true
        Log.d(TAG, "Mock模式初始化完成")
        return true
    }

    fun initRealMode(): Boolean {
        // TODO: 初始化真实环境
        isInitialized = true
        Log.d(TAG, "Real模式初始化完成")
        return true
    }

    fun startListening() {
        if (!isInitialized) {
            Log.e(TAG, "未初始化")
            recognitionCallback?.onError(-1, "语音功能未初始化")
            return
        }

        if (mockManager != null) {
            mockManager?.startListening()
        } else {
            // TODO: 通过真实SDK开启监听
        }
    }

    fun stopListening() {
        if (mockManager != null) {
            mockManager?.stopListening()
        } else {
            // TODO: 通过真实SDK结束监听
        }
    }

    fun cancelListening() {
        if (mockManager != null) {
            mockManager?.cancelListening()
        } else {
            // TODO: 真实SDK
        }
    }

    fun init(): Boolean {
        if (isInitialized) {
            Log.w(TAG, "VoiceManager已经初始化完成")
            return true
        }
        return if (SpeechConfig.mockMode) {
            initMockMode()
        } else {
            initRealMode()
        }
    }

    interface RecognitionCallback {
        fun onReady()
        fun onBegin()
        fun onVolumeChanged(volume: Int)
        fun onResult(text: String)
        fun onEnd()
        fun onError(errorCode: Int, errorMessage: String)
    }

    companion object {
        const val TAG = "VoiceAssistantManger"
    }
}