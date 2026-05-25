package com.max.voiceassistant.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.max.voiceassistant.DialogAdapter
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.max.voiceassistant.R
import com.max.voiceassistant.data.AppSettings
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
    private val appSettings by lazy { AppSettings(applicationContext) }

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
            viewModel.recognizedText.collectLatest {
                updateRecognizedTextUI(it)
            }
        }

        lifecycleScope.launch {
            viewModel.volume.collectLatest {
                updateVolumeUI(it)
            }
        }
    }

    private fun updateRecognizedTextUI(text: String) {
        if (text.isNotEmpty()) {
            binding.tvRecognizedText.visibility = View.VISIBLE
            binding.tvRecognizedText.text = text
        } else {
            binding.tvRecognizedText.visibility = View.GONE
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
                RecognitionState.RECOGNIZING, RecognitionState.PROCESS -> {}
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
        // 设置按钮
        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }
    }

    /**
     * 显示设置对话框：语音模式、清空历史、关于。
     */
    private fun showSettingsDialog() {
        val currentMode = if (appSettings.useMockMode) getString(R.string.settings_mode_mock) else getString(R.string.settings_mode_real)
        val items = arrayOf(
            getString(R.string.settings_voice_mode, currentMode),
            getString(R.string.settings_clear_history),
            getString(R.string.settings_about)
        )

        AlertDialog.Builder(this).setTitle(R.string.settings_title).setItems(items) { _, which ->
            when (which) {
                0 -> showModeSelectionDialog()
                1 -> {
                    viewModel.clearDialog()
                    Toast.makeText(this, getString(R.string.settings_history_cleared), Toast.LENGTH_SHORT).show()
                }
                2 -> showAboutDialog()
            }
        }.show()
    }

    /**
     * 显示语音模式选择（模拟 / 真实），切换后需重启生效。
     */
    private fun showModeSelectionDialog() {
        val modes = arrayOf(getString(R.string.settings_mode_mock_label), getString(R.string.settings_mode_real_label))
        val currentIndex = if (appSettings.useMockMode) 0 else 1

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_select_mode_title)
            .setSingleChoiceItems(modes, currentIndex) { dialog, which ->
                val newMockMode = (which == 0)
                if (newMockMode != appSettings.useMockMode) {
                    appSettings.useMockMode = newMockMode
                    val modeName = if (newMockMode) getString(R.string.settings_mode_mock) else getString(R.string.settings_mode_real)
                    Toast.makeText(this, getString(R.string.settings_switched_mode, modeName), Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    /**
     * 显示关于：应用名、当前模式、功能列表与提示。
     */
    private fun showAboutDialog() {
        val modeText = if (viewModel.isMockMode()) "模拟模式" else "真实模式"
        AlertDialog.Builder(this).setTitle("关于").setMessage(
            getString(R.string.about_message, modeText)
        ).setPositiveButton(R.string.common_ok, null).show()
    }

    private fun checkPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                AlertDialog.Builder(this).setTitle("录音权限")
                    .setMessage("获取录音权限")
                    .setPositiveButton("授权") { _, _ ->
                        requestPermissionLauncher.launch(
                            arrayOf(Manifest.permission.RECORD_AUDIO)
                        )
                    }
            }

            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.RECORD_AUDIO)
                )
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // 权限均授予，开始录音
            startVoiceRecognition()
        } else {
            Toast.makeText(this, "权限获取失败，录音功能无法使用，请授权", Toast.LENGTH_LONG).show()
        }
    }

    private fun startVoiceRecognition() {
        viewModel.startListening()
    }

}