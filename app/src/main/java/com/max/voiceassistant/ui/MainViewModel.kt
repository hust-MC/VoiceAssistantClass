package com.max.voiceassistant.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.max.voiceassistant.IntentParser
import com.max.voiceassistant.data.DialogRepository
import com.max.voiceassistant.data.VehicleStateRepository
import com.max.voiceassistant.executor.CommandExecutor
import com.max.voiceassistant.model.CommandResult
import com.max.voiceassistant.model.DialogMessage
import com.max.voiceassistant.model.RecognitionState
import com.max.voiceassistant.model.VehicleState
import com.max.voiceassistant.speech.VoiceAssistantManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val context: Context) : ViewModel() {

    private val _recognitionState = MutableStateFlow(RecognitionState.IDLE)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()

    private val vehicleRepository = VehicleStateRepository()
    private val dialogRepository = DialogRepository()

    val vehicleState: StateFlow<VehicleState> = vehicleRepository.vehicleState
    val dialogMessages: StateFlow<List<DialogMessage>> = dialogRepository.message

    private val _volume = MutableStateFlow(0)
    val volume: StateFlow<Int> = _volume.asStateFlow()

    private val _lastResult = MutableStateFlow<CommandResult?>(null)
    val lastResult: StateFlow<CommandResult?> = _lastResult.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val intentParser = IntentParser()
    private val commandExecutor = CommandExecutor(context, vehicleRepository)

    private val voiceManager = VoiceAssistantManager(context)

    init {
        dialogRepository.addAssistantMessage("您好，我是您的车载语音助手，请问有什么可以帮您")
        initVoiceManager()
    }

    private fun initVoiceManager() {
        voiceManager.init()
        voiceManager.setRecognitionCallback(object : VoiceAssistantManager.RecognitionCallback {
            override fun onReady() {
                _recognitionState.value = RecognitionState.LISTENING
            }

            override fun onBegin() {
                _recognitionState.value = RecognitionState.LISTENING
                _recognizedText.value = ""
            }

            override fun onVolumeChanged(volume: Int) {
                _volume.value = volume
            }

            override fun onResult(text: String) {
                _recognitionState.value = RecognitionState.RECOGNIZING
                _recognizedText.value = text
                processUserInput(text)
            }

            override fun onPartialResult(text: String) {
                _recognizedText.value = text
            }

            override fun onEnd() {
                if (_recognitionState.value != RecognitionState.PROCESS) {
                    _recognitionState.value = RecognitionState.IDLE
                }
                _volume.value = 0
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                _recognitionState.value = RecognitionState.ERROR
                _lastResult.value = CommandResult.Error("识别失败：${errorMessage}")

                viewModelScope.launch {
                    kotlinx.coroutines.delay(3000)
                    _recognitionState.value = RecognitionState.IDLE
                    _lastResult.value = null
                }
            }
        })
    }

    fun processUserInput(text: String) {
        if (text.isBlank()) {
            return
        }

        viewModelScope.launch {
            // 1. 添加用户消息到对话
            dialogRepository.addUserMessage(text)
            // 2. 解析意图
            _recognitionState.value = RecognitionState.RECOGNIZING
            val command = intentParser.parse(text)
            // 3. 执行命令
            val result = commandExecutor.execute(command)
//            // 4. 临时：直接测试空调控制等
//            val result = handleTestCommand(text)

            // 5. 添加助手回复

            val responseText = when (result) {
                is CommandResult.Success -> result.message
                is CommandResult.Error -> result.message
            }

            dialogRepository.addAssistantMessage(responseText)
            // 6. 更新最近结果（用于UI反馈动画)
            _lastResult.value = result
            // 7. 朗读处理结果
            voiceManager.speak(responseText)
        }

    }

    private fun handleTestCommand(text: String): CommandResult {
        return when {
            text.contains("打开空调") -> {
                vehicleRepository.setACOn(true)
                CommandResult.Success("已为您打开空调")
            }

            text.contains("关闭空调") -> {
                vehicleRepository.setACOn(false)
                CommandResult.Success("已为您关闭空调")
            }

            else -> {
                vehicleRepository.setACOn(false)
                CommandResult.Success("已为您关闭空调")
            }
        }
    }

    fun startListening() {
        _recognitionState.value = RecognitionState.LISTENING
        voiceManager.startListening()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun cancelListening() {
        voiceManager.cancelListening()
        _recognitionState.value = RecognitionState.IDLE
    }

    fun clearDialog() {
        dialogRepository.clearMessage()
        dialogRepository.addAssistantMessage("对话已清空，请问有什么可以帮您")
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                MainViewModel(context) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

}