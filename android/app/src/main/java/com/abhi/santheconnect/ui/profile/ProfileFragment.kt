package com.abhi.santheconnect.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.abhi.santheconnect.R
import com.abhi.santheconnect.databinding.FragmentProfileBinding
import com.abhi.santheconnect.ui.auth.AuthActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGotoLogin.setOnClickListener {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            binding.layoutAuthenticated.visibility = View.VISIBLE
            binding.layoutUnauthenticated.visibility = View.GONE
            
            binding.tvUserName.text = user.displayName ?: "Santhe User"
            binding.tvUserEmail.text = user.email
            
            if (user.photoUrl != null) {
                binding.ivProfilePic.load(user.photoUrl) {
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_image_placeholder)
                }
            } else {
                binding.ivProfilePic.setImageResource(R.drawable.ic_image_placeholder)
            }
        } else {
            binding.layoutAuthenticated.visibility = View.GONE
            binding.layoutUnauthenticated.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
