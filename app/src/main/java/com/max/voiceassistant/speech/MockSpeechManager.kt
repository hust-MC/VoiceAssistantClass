package com.max.voiceassistant.speech

import android.os.Handler
import android.os.Looper
import android.util.Log

class MockSpeechManager {

    private var isListening = false
    private var recognitionListener: SpeechRecognizerManager.RecognitionListener? = null
    private val handler = Handler(Looper.getMainLooper())

    fun setRecognitionListener(listener: SpeechRecognizerManager.RecognitionListener) {
        recognitionListener = listener
    }

    /**
     * 开始监听
     */
    fun startListening() {
        if (isListening) {
            Log.w(TAG, "已经在监听了")
            return
        }

        isListening = true

        Log.d(TAG, "开始Mock监听")
        handler.post {
            recognitionListener?.onReady()
        }

        // 模拟Ready延时
        handler.postDelayed({
            if (isListening) {
                recognitionListener?.onBegin()
            }
        }, 200)
        simulateVolumeChanges()
    }

    /**
     * 模拟监听中动画
     */
    private fun simulateVolumeChanges() {
        var volume = 0
        val runnable = object : Runnable {
            override fun run() {
                if (isListening) {
                    volume = (Math.random() * 100).toInt()
                    recognitionListener?.onVolumeChanged(volume)
                    handler.postDelayed(this, 100)
                }
            }
        }

        handler.postDelayed(runnable, 300)
    }

    /**
     * 结束监听，并返回结果
     */
    fun stopListening(): String {
        if (!isListening) {
            return ""
        }

        isListening = false
        Log.d(TAG, "停止Mock")

        // 返回模拟结果
        val mockResults = listOf(
            "打开空调",
            "关闭空调",
            // "打开音乐"
        )

        // 模拟语音识别结果
        val result = mockResults.random()

        handler.post {
            recognitionListener?.onResult(result)
            recognitionListener?.onEnd()
        }

        return result
    }

    fun cancelListening() {
        isListening = false
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "取消Mock监听")
    }

    companion object {
        const val TAG = "MockSpeechManager"
    }
}