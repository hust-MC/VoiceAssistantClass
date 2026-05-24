package com.max.voiceassistant.data

import com.max.voiceassistant.model.DialogMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DialogRepository {
    private val _messages = MutableStateFlow<List<DialogMessage>>(emptyList())

    val message: StateFlow<List<DialogMessage>> = _messages.asStateFlow()


    /**
     * 添加用户消息
     */
    fun addUserMessage(content: String) {
        addMessage(DialogMessage.User(content))
    }

    /**
     * 添加助手消息
     */
    fun addAssistantMessage(content: String) {
        addMessage(DialogMessage.Assistant(content))
    }

    /**
     * 添加消息
     */
    private fun addMessage(message: DialogMessage) {
        _messages.value += message
    }

    fun clearMessage() {
       _messages.value = emptyList()
    }

}