package com.max.voiceassistant.model

/**
 * 车辆状态模型
 */
data class VehicleState(
    val ac: ACState = ACState(),
    val door: DoorState = DoorState(),
    val engine: EngineState = EngineState()
)

/**
 * 空调状态
 */
data class ACState(
    val isOn:Boolean = false,
    val temperature: Int = 24,
)

/**
 * 车门状态
 */
data class DoorState(
    val isLocked: Boolean = true
)

/**
 * 发动机状态
 */
data class EngineState(
    val isRunning: Boolean = false
)