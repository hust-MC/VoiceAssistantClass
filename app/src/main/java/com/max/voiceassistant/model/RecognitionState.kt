package com.max.voiceassistant.model

enum class RecognitionState {
    /**  空闲，可开始录音 */
    IDLE,
    /** 正在录音 */
    LISTENING,
    /** 正在识别 */
    RECOGNIZING,
    /** 正在处理命令 */
    PROCESS,
    /** 识别失败 */
    ERROR,
}