package com.abhi.santheconnect.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhi.santheconnect.R
import com.abhi.santheconnect.databinding.FragmentChatBinding
import com.google.android.material.chip.Chip

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private val adapter = ChatAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChatList()
        setupSuggestions()
        setupInput()
        observeViewModel()
    }

    private fun setupChatList() {
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = adapter
    }

    private fun setupSuggestions() {
        val suggestions = listOf(
            "What is a 'Santhe'?",
            "Best places for Silk sarees?",
            "Famous food in Dharwad?",
            "Traditional toys from Karnataka?"
        )

        suggestions.forEach { text ->
            val chip = Chip(requireContext()).apply {
                this.text = text
                setChipBackgroundColorResource(R.color.white)
                setChipStrokeColorResource(R.color.ochre)
                chipStrokeWidth = 2f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.ochre_dark))
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                setOnClickListener { viewModel.sendMessage(text) }
            }
            binding.suggestionContainer.addView(chip)
        }
    }

    private fun setupInput() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                binding.etMessage.setText("")
            }
        }
        
        // Scroll to bottom when focused
        binding.etMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && adapter.itemCount > 0) {
                binding.rvChat.postDelayed({
                    binding.rvChat.smoothScrollToPosition(adapter.itemCount - 1)
                }, 300)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                binding.rvChat.scrollToPosition(messages.size - 1)
            }
            binding.suggestionsLayout.visibility = if (messages.size > 2) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled = !loading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
