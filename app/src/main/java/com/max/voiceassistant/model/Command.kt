package com.max.voiceassistant.model

/**
 * 命令数据类
 */
data class Command(
    val type: CommandType,
    val category: CommandCategory,
    val params: Map<String, String> = emptyMap()
)

/**
 * 定义命令类型
 */
enum class CommandType {
    // 空调控制
    AC_ON, AC_OFF, AC_TEMP_UP, AC_TEMP_DOWN, AC_MODE_COOL, AC_MODE_HEAT,

    // 车门控制
    DOOR_LOCK, DOOR_UNLOCK,

    // 媒体控制
    MEDIA_PLAY, MEDIA_PAUSE,

    // 其他
    OTHERS
}

/**
 * 定义命令分类
 */
enum class CommandCategory {
    VEHICLE, // 车辆控制
    MEDIA,   // 媒体控制
    QUERY,   // 查询
    UNKNOWN, // 未知
}