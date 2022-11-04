package ru.aasmc.petfinderapp.sharing.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.common.utils.setImage
import ru.aasmc.petfinderapp.sharing.databinding.FragmentSharingBinding

@AndroidEntryPoint
class SharingFragment : Fragment() {
    companion object {
        const val ANIMAL_ID = "id"
    }

    private var _binding: FragmentSharingBinding? = null
    private val binding: FragmentSharingBinding
        get() = _binding!!

    private val viewModel: SharingFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharingBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        subscribeToViewStateUpdates()
    }

    private fun setupUI() {
        val animalId = requireArguments().getLong(ANIMAL_ID)
        viewModel.onEvent(SharingEvent.GetAnimalToShare(animalId))

        binding.shareButton.setOnClickListener {
            Snackbar.make(requireView(), "Shared! Or not :]", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun subscribeToViewStateUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { render(it) }
            }
        }
    }

    private fun render(viewState: SharingViewState) {
        val (image, message) = viewState.animalToShare
        binding.image.setImage(image)
        binding.messageToShareEditText.setText(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}