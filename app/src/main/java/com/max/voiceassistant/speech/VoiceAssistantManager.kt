package com.max.voiceassistant.speech

import android.content.Context
import android.util.Log

class VoiceAssistantManager(private val context: Context) {

    private var mockManager: MockSpeechManager? = null
    private var recognitionCallback: RecognitionCallback? = null
    private var isInitialized = false
    private var speechRecognizer:SpeechRecognizerManager? = null

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

            override fun onPartialResult(partialResult: String) {
                recognitionCallback?.onPartialResult(partialResult)
            }
        })

        isInitialized = true
        Log.d(TAG, "Mock模式初始化完成")
        return true
    }

    private fun initRealMode(): Boolean {
        Log.d(TAG, "初始化Real模式")

        speechRecognizer = SpeechRecognizerManager(context).apply {
            init()
            setListener(object : SpeechRecognizerManager.RecognitionListener {
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

                override fun onPartialResult(partialResult: String) {
                    recognitionCallback?.onPartialResult(partialResult)
                }

            })
        }

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
            speechRecognizer?.startListening()
        }
    }

    fun stopListening() {
        if (mockManager != null) {
            mockManager?.stopListening()
        } else {
            speechRecognizer?.stopListening()
        }
    }

    fun cancelListening() {
        if (mockManager != null) {
            mockManager?.cancelListening()
        } else {
            speechRecognizer?.cancel()
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
        fun onPartialResult(text: String)
        fun onEnd()
        fun onError(errorCode: Int, errorMessage: String)
    }

    companion object {
        const val TAG = "VoiceAssistantManger"
    }
}