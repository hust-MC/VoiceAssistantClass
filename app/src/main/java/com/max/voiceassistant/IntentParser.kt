package com.max.voiceassistant

import com.max.voiceassistant.model.Command
import com.max.voiceassistant.model.CommandCategory
import com.max.voiceassistant.model.CommandType

// 详细请参考资料包
class IntentParser {
    fun parse(text: String): Command {
        val normalizerText = text.lowercase().trim()
        return when {
            isMediaCommand(normalizerText) -> parseMediaCommand(normalizerText)
            isVehicleCommand(normalizerText) -> parseVehicleCommand(normalizerText)
            // .... 详细解析方式，可参考资料包源码
            else -> Command(CommandType.OTHERS, CommandCategory.UNKNOWN)
        }
    }

    private fun isMediaCommand(text: String): Boolean {
        val keywords = listOf(
            "播放",
            "暂停",
            "停止",
            "音乐",
            "歌",
            "歌曲",
            "下一首",
            "上一首",
            "上一曲",
            "下一曲",
            "切歌",
            "音量"
        )
        return keywords.any { text.contains(it) }
    }

    private fun isVehicleCommand(text: String): Boolean {
        val keywords = listOf(
            "空调",
            "温度",
            "大灯",
            "座椅",
            "风速",
            "冷风",
            "前窗",
            "熄火",
            "启动",
            "后备箱",
            "车门",
            "加热"
        )
        return keywords.any { text.contains(it) }
    }

    private fun parseMediaCommand(text: String): Command {
        return when {
            // 播放
            containsAny(text, "播放", "放歌", "听歌", "放音乐", "听音乐") &&
                    !containsAny("暂停", "停止", "不要") ->
                Command(CommandType.MEDIA_PLAY, CommandCategory.MEDIA)

            // 暂停
            containsAny(text, "暂停", "停止", "不要") ->
                Command(CommandType.MEDIA_PAUSE, CommandCategory.MEDIA)

            else -> {
                Command(CommandType.OTHERS, CommandCategory.UNKNOWN)
            }
        }
    }

    private fun parseVehicleCommand(text: String): Command {
        return when {
            // 空调相关
            containsAny(
                text,
                "空调",
                "温度",
                "风速",
                "制热",
                "制冷",
                "暖风"
            ) -> parseACCommand(text)

            else -> {
                Command(CommandType.OTHERS, CommandCategory.UNKNOWN)
            }
        }
    }

    private fun parseACCommand(text: String): Command {
        return when {
            // 打开空调
            containsAny(text, "打开", "开启", "开") && text.contains("空调") ->
                Command(CommandType.AC_ON, CommandCategory.VEHICLE)
            // 关闭空调
            containsAny(text, "关闭", "关掉", "关") && text.contains("空调") ->
                Command(CommandType.AC_OFF, CommandCategory.VEHICLE)
            // 调节温度
            // 改变模式
            else -> {
                Command(CommandType.OTHERS, CommandCategory.UNKNOWN)
            }
        }
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it) }
    }
}