package com.abhi.santheconnect.ui.reviews

import android.Manifest
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhi.santheconnect.databinding.FragmentReviewWallBinding
import com.abhi.santheconnect.utils.showToast
import java.io.File
import java.io.IOException

class ReviewWallFragment : Fragment() {

    private var _binding: FragmentReviewWallBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewViewModel by viewModels()
    private val adapter = ReviewAdapter()

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startRecording()
        else requireContext().showToast("Microphone permission required for voice reviews")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReviewWallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter

        binding.btnRecord.setOnClickListener {
            if (isRecording) stopRecording() else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        observeViewModel()
        viewModel.loadAllReviews()
    }

    private fun startRecording() {
        // Use .m4a extension for AAC audio
        audioFile = File(requireContext().cacheDir, "review_${System.currentTimeMillis()}.m4a")
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireContext())
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile!!.absolutePath)
            setMaxDuration(60_000)
            try {
                prepare()
                start()
                isRecording = true
                binding.btnRecord.text = "⏹ Stop Recording"
                binding.tvRecordStatus.text = "Recording… (max 60s)"
                binding.tvRecordStatus.visibility = View.VISIBLE
            } catch (e: IOException) {
                requireContext().showToast("Failed to start recording")
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Handle early stops
        }
        mediaRecorder = null
        isRecording = false
        binding.btnRecord.text = "🎙 Record Voice Review"
        binding.tvRecordStatus.text = "Processing with Gemini AI…"

        audioFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                viewModel.transcribeAndSubmitVoiceReview(
                    vendorId = "general",
                    author = "Traveler",
                    audioBytes = file.readBytes()
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            adapter.submitList(reviews)
            binding.tvEmpty.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.transcription.observe(viewLifecycleOwner) { text ->
            binding.tvRecordStatus.text = "✅ Transcribed: \"$text\""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaRecorder?.release()
        _binding = null
    }
}
