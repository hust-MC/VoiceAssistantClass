package com.max.voiceassistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.max.voiceassistant.DialogAdapter
import com.max.voiceassistant.R
import com.max.voiceassistant.databinding.ActivityMainBinding
import com.max.voiceassistant.model.ACState
import com.max.voiceassistant.model.DoorState
import com.max.voiceassistant.model.RecognitionState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogAdapter: DialogAdapter

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupClickListeners()

    }

    private fun setupUI() {
        // 初始化对话列表
        dialogAdapter = DialogAdapter()
        binding.dialog.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
            }
            adapter = dialogAdapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.dialogMessages.collectLatest {
                dialogAdapter.submitList(it)
                // 滚动到底部
                if (it.isNotEmpty()) {
                    binding.dialog.smoothScrollToPosition(it.size - 1)
                }
            }
        }

        // 观察识别文本
        lifecycleScope.launch {
            viewModel.recognizedText.collectLatest { text ->
                updateRecognizedTextUI(text)
            }
        }

        lifecycleScope.launch {
            viewModel.vehicleState.collectLatest {
                updateACUI(it.ac)
                updateDoorUI(it.door)
            }
        }

        lifecycleScope.launch {
            viewModel.recognitionState.collectLatest {
                updateMicrophoneUI(it)
            }
        }

        lifecycleScope.launch {
            viewModel.volume.collectLatest {
                updateVolumeUI(it)
            }
        }
    }

    private fun updateVolumeUI(volume: Int) {
        if (volume > 0) {
            val alpha = 0.5f + (volume / 100f) * 0.5f
            binding.fabMicrophone.alpha = alpha.coerceIn(0.5f, 1f)
        } else {
            binding.fabMicrophone.alpha = 1f
        }
    }

    private fun updateACUI(acState: ACState) {
        if (acState.isOn) {
            binding.tvACStatus.text = "空调 开"
            binding.chipAC.text = "关闭空调"
            binding.tvACStatus.setTextColor(getColor(R.color.text_primary))
            binding.iconAC.setColorFilter(getColor(R.color.accent_blue))
            binding.tvTemperature.setTextColor(getColor(R.color.text_primary))
//            binding.iconAC.setImageResource("响应模式的icon")
        } else {
            binding.tvACStatus.text = "空调 关"
            binding.chipAC.text = "打开空调"
            binding.tvACStatus.setTextColor(getColor(R.color.text_secondary))
            binding.iconAC.setColorFilter(getColor(R.color.text_secondary))
            binding.tvTemperature.setTextColor(getColor(R.color.text_secondary))

//            binding.iconAC.setImageResource("响应模式的icon")
        }

        Log.w("MainViewModel", "打开空调")
    }

    /**
     * 显示或隐藏识别到的文字。
     *
     * @param text 识别结果，空则隐藏
     */
    private fun updateRecognizedTextUI(text: String) {
        if (text.isNotEmpty()) {
            binding.tvRecognizedText.visibility = View.VISIBLE
            binding.tvRecognizedText.text = text
        } else {
            binding.tvRecognizedText.visibility = View.GONE
        }
    }


    private fun updateDoorUI(doorState: DoorState) {
        binding.tvDoorStatus.text = if (doorState.isLocked) {
            "车门 已锁"
        } else {
            "车门  已解锁"
        }
    }

    private fun updateMicrophoneUI(state: RecognitionState) {

        when (state) {
            RecognitionState.IDLE -> {
                binding.fabMicrophone.setImageResource(R.drawable.ic_mic)
                binding.tvRecognitionStatus.text = "点击麦克风开始说话"
                binding.fabMicrophone.isEnabled = true
            }

            RecognitionState.LISTENING -> {
                binding.tvRecognitionStatus.text = "正在聆听"
                binding.fabMicrophone.setImageResource(R.drawable.ic_mic)
                binding.fabMicrophone.isEnabled = true
            }

            RecognitionState.RECOGNIZING -> {
                binding.tvRecognitionStatus.text = "识别中"
                binding.fabMicrophone.isEnabled = false
            }

            RecognitionState.PROCESS -> {
                binding.tvRecognitionStatus.text = "处理中"
                binding.fabMicrophone.isEnabled = false
            }

            RecognitionState.ERROR -> {
                binding.tvRecognitionStatus.text = "识别失败，点击重试"
                binding.fabMicrophone.setImageResource(R.drawable.ic_mic)
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabMicrophone.setOnClickListener {
            val currentState = viewModel.recognitionState.value
            when (currentState) {
                RecognitionState.IDLE -> checkPermissionAndStart()
                RecognitionState.LISTENING -> viewModel.stopListening()
                RecognitionState.RECOGNIZING, RecognitionState.PROCESS -> { }
                RecognitionState.ERROR -> checkPermissionAndStart()
            }
        }

        // 快捷指令
        binding.chipPlayMusic.setOnClickListener {
            viewModel.processUserInput("播放音乐")
        }

        binding.chipAC.setOnClickListener {
            viewModel.processUserInput(binding.chipAC.text.toString())
        }

        binding.chipTime.setOnClickListener {
            viewModel.processUserInput("")

        }
    }
    /**
     * 检查录音权限：已有则直接开始；需说明则弹窗后请求；否则直接请求。
     */
    private fun checkPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                AlertDialog.Builder(this).setTitle(R.string.permission_record_title)
                    .setMessage(R.string.permission_record_message)
                    .setPositiveButton(R.string.permission_grant) { _, _ ->
                        requestPermissionLauncher.launch(
                            arrayOf(Manifest.permission.RECORD_AUDIO)
                        )
                    }.setNegativeButton(R.string.common_cancel, null).show()
            }

            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.RECORD_AUDIO)
                )
            }
        }
    }

    /** 录音权限请求 Launcher */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // 权限已授予，开始录音
            startVoiceRecognition()
        } else {
            // 权限被拒绝
            Toast.makeText(this, getString(R.string.permission_need_record), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 调用 ViewModel 开始语音识别。
     */
    private fun startVoiceRecognition() {
        viewModel.startListening()
    }

}