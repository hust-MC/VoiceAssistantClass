package com.max.voiceassistant.model

sealed class DialogMessage {
    abstract val content: String
    abstract val timestamp: Long

    /** 用户发送的消息 */
    data class User(
        override val content: String,
        override val timestamp: Long = System.currentTimeMillis()
    ): DialogMessage()

    /** 语音助手的消息 */
    data class Assistant(
        override val content: String,
        override val timestamp: Long = System.currentTimeMillis()
    ): DialogMessage()
}