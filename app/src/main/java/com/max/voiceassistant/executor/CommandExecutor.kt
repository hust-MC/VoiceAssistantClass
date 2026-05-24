package com.max.voiceassistant.executor

import android.content.Context
import com.max.voiceassistant.data.VehicleStateRepository
import com.max.voiceassistant.model.Command
import com.max.voiceassistant.model.CommandCategory
import com.max.voiceassistant.model.CommandResult

// 一级分发器
class CommandExecutor(
    private val context: Context,
    vehicleStateRepository: VehicleStateRepository
) {
    private val vehicleExecutor = VehicleControlExecutor(context, vehicleStateRepository)


    fun execute(command: Command) : CommandResult {
        return when(command.category) {
            // CommandCategory.MEDIA -> // mediaExecutor.execute(command)
            CommandCategory.VEHICLE -> vehicleExecutor.execute(command)
            // CommandCategory.QUERY -> // queryExecutor.execute(command)
            else -> CommandResult.Error("抱歉，我没听清楚，您可以说：打开空调、播放音乐、现在几点")
        }
    }
}