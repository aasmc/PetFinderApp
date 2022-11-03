package ru.aasmc.petfinderapp.onboarding.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.databinding.FragmentOnboardingBinding

@AndroidEntryPoint
class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding: FragmentOnboardingBinding
        get() = _binding!!

    private val viewModel by viewModels<OnboardingFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewStateUpdates()
        observeViewEffects()
    }

    private fun setupUI() {
        setupPostcodeTextField()
        setupDistanceTextField()
        listenToSubmitButton()
    }

    private fun setupPostcodeTextField() {
        binding.postcodeInputText.doAfterTextChanged {
            viewModel.onEvent(OnboardingEvent.PostcodeChanged(it!!.toString()))
        }
    }

    private fun setupDistanceTextField() {
        binding.maxDistanceInputText.doAfterTextChanged {
            viewModel.onEvent(OnboardingEvent.DistanceChanged(it!!.toString()))
        }
    }

    private fun listenToSubmitButton() {
        binding.onboardingSubmitButton.setOnClickListener {
            viewModel.onEvent(OnboardingEvent.SubmitButtonClicked)
        }
    }

    private fun observeViewStateUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect {
                    render(it)
                }
            }
        }
    }

    private fun render(state: OnboardingViewState) {
        with(binding) {
            postcodeTextInputLayout.error = resources.getString(state.postcodeError)
            maxDistanceTextInputLayout.error = resources.getString(state.distanceError)
            onboardingSubmitButton.isEnabled = state.submitButtonActive
        }
    }

    private fun observeViewEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewEffects.collect {
                    reactTo(it)
                }
            }
        }
    }

    private fun reactTo(effect: OnboardingViewEffect) {
        when(effect) {
            OnboardingViewEffect.NavigateToAnimalsNearYou -> navigateToAnimalsNearYou()
        }
    }

    private fun navigateToAnimalsNearYou() {
        findNavController().navigate(R.id.action_onboardingFragment_to_animalsNearYou)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}