package com.max.voiceassistant.model


/**
 * 命令结果
 */
sealed class CommandResult {
    data class Success(val message: String) : CommandResult()
    data class Error(val message: String) : CommandResult()
}