package com.max.voiceassistant.data

import com.max.voiceassistant.model.ACState
import com.max.voiceassistant.model.VehicleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 车辆状态仓库
 * 管理车辆的所有状态数据
 */
class VehicleStateRepository {

    // 私有可变状态
    private val _vehicleState = MutableStateFlow(VehicleState())

    // 公开只读状态
    val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    //======= 空调控制 =====
    fun setACOn(isOn: Boolean) {
        _vehicleState.value = _vehicleState.value.copy(
            ac = _vehicleState.value.ac.copy(isOn = isOn)
        )
    }

    // ========= 车门控制 ========
    fun setDoorLocked(isLocked: Boolean) {
        _vehicleState.value = _vehicleState.value.copy(
            door = _vehicleState.value.door.copy(isLocked = isLocked)
        )
    }

    //======== 发动机控制 =========
    fun setEngineRunning(isRunning: Boolean) {
        _vehicleState.value = _vehicleState.value.copy(
            engine = _vehicleState.value.engine.copy(isRunning = isRunning)
        )
    }

    fun getCurrentState(): VehicleState = _vehicleState.value

    fun updateACState(acState: ACState) {
        _vehicleState.value = _vehicleState.value.copy(ac = acState)
    }
}