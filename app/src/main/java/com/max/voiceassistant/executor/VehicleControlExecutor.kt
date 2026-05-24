package com.max.voiceassistant.executor

import android.content.Context
import com.max.voiceassistant.data.VehicleStateRepository
import com.max.voiceassistant.model.Command
import com.max.voiceassistant.model.CommandResult
import com.max.voiceassistant.model.CommandType

class VehicleControlExecutor(
    private val context: Context,
    private val repository: VehicleStateRepository
) {
    fun execute(command: Command): CommandResult {
        return when (command.type) {
            CommandType.AC_ON -> executeACON()
            CommandType.AC_OFF -> executeACOFF()
            // CommandType.AC_TEMP_UP -> executeACTempUp()
            // 。。。自行补充，详细可以参考资料包源码
            else -> CommandResult.Error("暂不支持该功能")
        }
    }

    private fun executeACON(): CommandResult {
        val currentAC = repository.getCurrentState().ac
        if (currentAC.isOn) {
            return CommandResult.Success("空调已经打开了，当前为${currentAC.temperature}度")
        }

        // 调用底层SDK，来实际打开空调
        // AC.open()
        repository.updateACState(currentAC.copy(isOn = true, temperature = 24))
        return CommandResult.Success("空调已打开，当前温度为24度")
    }

    private fun executeACOFF() : CommandResult {
        val currentAC = repository.getCurrentState().ac
        if (!currentAC.isOn) {
            return CommandResult.Success("空调已经是关闭状态")
        }

        // 调用底层空调SDK，来实际关闭空调
        // AC.turnOff()

        repository.updateACState(currentAC.copy(isOn = false))
        return CommandResult.Success("空调已关闭")
    }

    //.. 调低温度
    //.. 改变风速
}