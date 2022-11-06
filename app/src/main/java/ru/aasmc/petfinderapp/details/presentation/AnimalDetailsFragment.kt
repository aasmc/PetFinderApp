package ru.aasmc.petfinderapp.details.presentation

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.common.presentation.model.UIAnimalDetailed
import ru.aasmc.petfinderapp.common.utils.setImage
import ru.aasmc.petfinderapp.common.utils.toEnglish
import ru.aasmc.petfinderapp.databinding.FragmentDetailsBinding

@AndroidEntryPoint
class AnimalDetailsFragment : Fragment() {
    companion object {
        const val ANIMAL_ID = "id"
    }

    private val FLING_SCALE = 1f

    private var _binding: FragmentDetailsBinding? = null
    private val binding: FragmentDetailsBinding
        get() = _binding!!

    private val viewModel: AnimalDetailsViewModel by viewModels()

    private var animalId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        animalId = requireArguments().getLong(ANIMAL_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        val event = AnimalDetailsEvent.LoadAnimalDetails(animalId!!)
        viewModel.onEvent(event)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is AnimalDetailsViewState.AnimalDetails -> {
                            displayPetDetails(state.animal)
                        }
                        AnimalDetailsViewState.Failure -> {
                            displayError()
                        }
                        AnimalDetailsViewState.Loading -> {
                            displayLoading()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun displayPetDetails(animalDetails: UIAnimalDetailed) {
        binding.group.isVisible = true
        stopAnimation()
        binding.name.text = animalDetails.name
        binding.description.text = animalDetails.description
        binding.image.setImage(animalDetails.photo)
        binding.sprayedNeutered.text = animalDetails.sprayNeutered.toEnglish()
        binding.specialNeeds.text = animalDetails.specialNeeds.toEnglish()

        val doubleTapGestureListener =
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    //TODO: start animation on double tap
                    return true
                }

                override fun onDown(e: MotionEvent) = true
            }
        val doubleTapGestureDetector =
            GestureDetector(requireContext(), doubleTapGestureListener)

        binding.image.setOnTouchListener { v, event ->
            doubleTapGestureDetector.onTouchEvent(event)
        }

        //TODO: start scaling Spring Animation

        //TODO: Create and set fling Gesture Listener

        //TODO: Add end listener for fling animation
    }

    private fun displayError() {
        startAnimation(R.raw.lazy_cat)
        binding.group.isVisible = false
        Snackbar.make(requireView(), R.string.an_error_occurred, Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun displayLoading() {
        startAnimation(R.raw.happy_dog)
        binding.group.isVisible = false
    }

    //TODO: add method parameter for animation resource
    private fun startAnimation(@RawRes animationRes: Int) {
        binding.loader.apply {
            isVisible = true
            setMinFrame(50)
            setMaxFrame(112)
            speed = 1.5f
            setAnimation(animationRes)
            playAnimation()
        }
        // adds custom color filter for the layer we want to modify
        // in this case - icon_circle
        binding.loader.addValueCallback(
            KeyPath("icon_circle", "**"),
            LottieProperty.COLOR_FILTER
        ) {
            PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun stopAnimation() {
        binding.loader.apply {
            cancelAnimation()
            isVisible = false
        }
    }

    private fun areViewsOverlapping(view1: View, view2: View): Boolean {
        val firstRect = Rect()
        view1.getHitRect(firstRect)

        val secondRect = Rect()
        view2.getHitRect(secondRect)

        return Rect.intersects(firstRect, secondRect)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
























