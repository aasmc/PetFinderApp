package ru.aasmc.petfinderapp.easteregg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.aasmc.petfinderapp.databinding.FragmentSecretBinding

class SecretFragment : Fragment() {
    private var _binding: FragmentSecretBinding? = null
    private val binding: FragmentSecretBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecretBinding.inflate(inflater, container, false)
        return binding.root
    }
}