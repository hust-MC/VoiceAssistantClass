package com.max.voiceassistant.speech

import android.content.Context
import android.util.Log
import com.baidu.tts.client.SpeechSynthesizer
import com.baidu.tts.client.SpeechSynthesizerListener
import com.baidu.tts.client.SynthesizerResponse
import com.baidu.tts.client.TtsEntity
import com.baidu.tts.client.TtsMode

class TTSManager(private val context: Context) {

    private var isInitialized = false
    private var listener: TTSListener? = null
    private var synthesizer: SpeechSynthesizer? = null

    private val synthesizerListener = object : SpeechSynthesizerListener {
        override fun onSynthesizeResponse(response: SynthesizerResponse?) {
            if (response == null) {
                Log.e(TAG, "相应为空")
                return
            }

            val utteranceID = response.utteranceId ?: ""
            val synthesizeType = response.synthesizeType
            val error = response.synthesizerError

            // 优先处理错误情况
            if (error != null) {
                val errorMsg = error.description ?: "未知错误（code: ${error.code}）"
                Log.e(TAG, "TTS发送错误：$errorMsg")
                listener?.onError(utteranceID, errorMsg)
                return
            }

            when (synthesizeType) {
                SynthesizerResponse.SynthesizeType.SYNTHESIZE_START -> {
                    Log.d(TAG, "SYNTHESIZE_START: $utteranceID")
                    listener?.onSynthesizeStart(utteranceID)
                }

                SynthesizerResponse.SynthesizeType.PLAY_START -> {
                    Log.d(TAG, "onSpeechStart: $utteranceID")
                    listener?.onSynthesizeStart(utteranceID)
                }

                SynthesizerResponse.SynthesizeType.PLAY_PROGRESS -> {
                    val data = response.synthesizerData
                    val progress = ((data?.audioPercent ?: 0f) * 100).toInt()
                    listener?.onSpeechProgress(utteranceID, progress)
                }

                SynthesizerResponse.SynthesizeType.SYNTHESIZE_FINISH -> {
                    Log.d(TAG, "SYNTHESIZE_FINISH: $utteranceID")
                }

                SynthesizerResponse.SynthesizeType.SYNTHESIZE_ERROR -> {
                    val errorMsg = error?.description ?: "合成错误"
                    Log.e(TAG, "Synthesize Error: $errorMsg")
                    listener?.onError(utteranceID, errorMsg)
                }

                else -> {
                    Log.d(TAG, "其他相应类似： $synthesizeType")
                }
            }
        }

    }

    fun init(): Boolean {
        if (isInitialized) {
            Log.w(TAG, "已经初始化完成")
        }

        // 1.创建合成器实例
        synthesizer = SpeechSynthesizer(context)

        // 2.设置API认证信息
        synthesizer?.setParam(SpeechSynthesizer.PARAM_APP_ID, SpeechConfig.APP_ID)
        synthesizer?.setParam(SpeechSynthesizer.PARAM_API_KEY, SpeechConfig.API_KEY)
        synthesizer?.setParam(SpeechSynthesizer.PARAM_SECRET_KEY, SpeechConfig.SECRET_KEY)

        // 3.设置监听器
        synthesizer?.setSpeechSynthesizerListener(synthesizerListener)

        // 4.设置合成参数
        setupParams()

        // 5.初始化引擎（加载在线TTS）
        val result = synthesizer?.loadOnlineTts()
        if (result == null || result.detailCode == 0) {
            isInitialized = true
            Log.d(TAG, "TTS 初始化成功")
            return true
        } else {
            val errorMsg = result.detailMessage ?: "未知错误"
            Log.e(TAG, "TTS初始化失败： $errorMsg:code:${result.detailCode}")
            return false
        }

    }

    private fun setupParams() {
        // 发言人（使用PARAM_ONLINE_SPEAKER）
        synthesizer?.setParam(
            SpeechSynthesizer.PARAM_ONLINE_SPEAKER,
            SpeechConfig.TTS.SPEAKER.toString()
        )

        // 语速
        synthesizer?.setParam(SpeechSynthesizer.PARAM_SPEED, SpeechConfig.TTS.SPEED.toString())
        // 音调
        synthesizer?.setParam(SpeechSynthesizer.PARAM_PITCH, SpeechConfig.TTS.PITCH.toString())
        // 音量
        synthesizer?.setParam(SpeechSynthesizer.PARAM_VOLUME, SpeechConfig.TTS.VOLUME.toString())
    }

    fun setListener(ttsListener: TTSListener) {
        listener = ttsListener
    }

    fun speak(text: String): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "TTS 还未初始化")
            return false
        }

        if (text.isBlank()) {
            Log.w(TAG, "Text is empty")
            return false
        }

        Log.d(TAG, "TTS 文本内容： $text")

        try {
            val ttsEntity = TtsEntity(text, TtsMode.ONLINE)
            val result = synthesizer?.speak(ttsEntity)

            if (result == null || result.detailCode == 0) {
                return true
            } else {
                Log.e(TAG, "TTS朗读失败:${result.detailMessage}, code: ${result.detailCode}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS error", e)
            return false
        }
    }

    interface TTSListener {
        fun onSynthesizeStart(utteranceID: String)
        fun onSpeechStart(utteranceID: String)
        fun onSpeechProgress(utteranceID: String, progress: Int)
        fun onSpeechFinish(utteranceID: String)
        fun onError(utteranceID: String, errorMsg: String)
    }

    companion object {
        private const val TAG = "TTSManager"
    }
}