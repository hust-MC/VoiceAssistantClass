package com.max.voiceassistant.speech

import android.content.Context
import android.util.Log
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import org.json.JSONObject

class SpeechRecognizerManager(private val context: Context) {

    private var asr: EventManager? = null
    private var isInitialized = false
    private var listener: RecognitionListener? = null
    private var isListening = false


    private val eventListener = EventListener { name, params, data, offset, length ->
        Log.d(TAG, "Event: $name, params: $params")
        when (name) {
            SpeechConstant.CALLBACK_EVENT_ASR_READY -> {
                listener?.onReady()
            }

            SpeechConstant.CALLBACK_EVENT_ASR_BEGIN -> {
                isListening = true
                listener?.onBegin()
            }

            SpeechConstant.CALLBACK_EVENT_ASR_VOLUME -> {
                try {
                    val json = JSONObject(params)
                    val volumePercent = json.optInt("volume-percent", 0)
                    listener?.onVolumeChanged(volumePercent)
                } catch (e: Exception) {
                    Log.e(TAG, "Parse Volume error", e)
                }
            }

            SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL -> {
                try {
                    if (params.isNullOrEmpty()) {
                        return@EventListener
                    }
                    when {
                        params.contains("\"partial_result\"") -> {
                            val json = JSONObject(params)
                            val resultArray = json.optJSONArray("results_recognition")
                            if (resultArray != null && resultArray.length() > 0) {
                                val partialResult = resultArray.optString(0, "")
                                if (partialResult.isNotEmpty()) {
                                    listener?.onPartialResult(partialResult)
                                }
                            }
                        }

                        params.contains("\"final_result\"") -> {
                            val json = JSONObject(params)
                            val resultArray = json.optJSONArray("results_recognition")
                            if (resultArray != null && resultArray.length() > 0) {
                                val result = resultArray.optString(0, "")
                                if (result.isNotEmpty()) {
                                    listener?.onResult(result)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解析失败", e)
                }
            }

            SpeechConstant.CALLBACK_EVENT_ASR_FINISH -> {
                isListening = false
                listener?.onEnd()
            }

            SpeechConstant.CALLBACK_EVENT_ASR_END -> {
                isListening = false
            }

            SpeechConstant.CALLBACK_EVENT_ASR_ERROR -> {
                isListening = false
                try {
                    val json = JSONObject(params)
                    val errorCode = json.optInt("error", -1)
                    val errorMsg = json.optString("desc", "未知错误")
                    listener?.onError(errorCode, errorMsg)
                } catch (e: Exception) {
                    Log.e(TAG, "初始化失败", e)
                }
            }
        }

    }

    fun init() {
        if (isInitialized) {
            Log.w(TAG, "当前已经初始化完成")
            return
        }
        try {
            asr = EventManagerFactory.create(context, "asr")
            asr?.registerListener(eventListener)
            isInitialized = true
            Log.d(TAG, "ASR初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "初始化失败", e)
        }
    }

    fun setListener(recognitionListener: RecognitionListener) {
        listener = recognitionListener
    }

    fun startListening() {
        if (!isInitialized) {
            listener?.onError(-1, "请先调用init()完成初始化")
            return
        }

        if (isListening) {
            return
        }

        // 构建识别参数
        val params = buildRecognitionParams()

        // 发送开始识别事件
        val json = JSONObject(params).toString()
        asr?.send(SpeechConstant.ASR_START, json, null, 0, 0)
        Log.d(TAG, "开始识别: $json")
    }

    fun stopListening() {
        if (!isListening) {
            return
        }
        isListening = false
        asr?.send(SpeechConstant.ASR_STOP, null, null, 0, 0)
        Log.d(TAG, "结束监听")

    }

    fun cancel() {
        asr?.send(SpeechConstant.ASR_CANCEL, "{}", null ,0,0)
        isListening = false
        Log.d(TAG, "取消监听")
    }

    fun release() {
        cancel()
        asr?.unregisterListener(eventListener)
        asr = null
        isListening = false
        isInitialized = false
        Log.d(TAG, "释放资源")

    }

    private fun buildRecognitionParams(): Map<String, Any> {
        val params = mutableMapOf<String, Any>()

        // 百度API认证信息
        params[SpeechConstant.APP_ID] = SpeechConfig.APP_ID
        params[SpeechConstant.APP_KEY] = SpeechConfig.API_KEY
        params[SpeechConstant.SECRET] = SpeechConfig.SECRET_KEY

        // 识别参数
        params[SpeechConstant.PID] = SpeechConfig.ASR.LANGUAGE
        params[SpeechConstant.VAD] = SpeechConstant.VAD_TOUCH
        params[SpeechConstant.ACCEPT_AUDIO_VOLUME] = true
        params[SpeechConstant.VAD_ENDPOINT_TIMEOUT] = SpeechConfig.ASR.VAD_ENDPOINT_TIMEOUT

        // 自行添加其它参数

        return params
    }

    interface RecognitionListener {
        fun onReady()
        fun onBegin()
        fun onVolumeChanged(volume: Int)
        fun onResult(result: String)
        fun onEnd()
        fun onError(errorCode: Int, errorMessage: String)
        fun onPartialResult(partialResult: String)
    }

    companion object {
        const val TAG = "SpeechRecognizerManager"
    }
}