package ru.aasmc.petfinderapp.details.presentation

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.SpringForce.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.common.presentation.model.UIAnimalDetailed
import ru.aasmc.petfinderapp.common.utils.setImage
import ru.aasmc.petfinderapp.common.utils.toEmoji
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

    private val springForce: SpringForce by lazy {
        SpringForce().apply {
            // describe how oscillations in a system decay after a disturbance
            dampingRatio = DAMPING_RATIO_HIGH_BOUNCY
            // The stiffer the spring is, the more force it applies to the attached
            // object when the spring is not at the final position.
            stiffness = STIFFNESS_VERY_LOW
        }
    }

    /**
     * Create a SpringAnimation for scaleX property.
     */
    private val callScaleXSpringAnimation: SpringAnimation by lazy {
        SpringAnimation(binding.call, DynamicAnimation.SCALE_X).apply {
            spring = springForce
        }
    }

    /**
     * Create a SpringAnimation for scaleY property.
     */
    private val callScaleYSpringAnimation: SpringAnimation by lazy {
        SpringAnimation(binding.call, DynamicAnimation.SCALE_Y).apply {
            spring = springForce
        }
    }

    private val FLING_FRICTION = 2f

    private val callFlingXAnimation: FlingAnimation by lazy {
        FlingAnimation(binding.call, DynamicAnimation.X).apply {
            friction = FLING_FRICTION
            setMinValue(0f)
            setMaxValue(binding.root.width.toFloat() - binding.call.width.toFloat())
        }
    }

    private val callFlingYAnimation: FlingAnimation by lazy {
        FlingAnimation(binding.call, DynamicAnimation.Y).apply {
            // the greater the friction is, the sooner the animation will slow down
            // 2.0f means, that it takes a bit of effort to fling the button onto the image
            friction = FLING_FRICTION
            setMinValue(0f)
            setMaxValue(binding.root.height.toFloat() - binding.call.width.toFloat())
        }
    }

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
                            displayPetDetails(state.animal, state.adopted)
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
    private fun displayPetDetails(animalDetails: UIAnimalDetailed, adopted: Boolean) {
        binding.call.scaleX = 0.6f
        binding.call.scaleY = 0.6f
        binding.call.isVisible = true
        binding.scrollView.isVisible = true
        stopAnimation()
        binding.name.text = animalDetails.name
        binding.description.text = animalDetails.description
        binding.image.setImage(animalDetails.photo)

        binding.sprayedNeutered.text = animalDetails.sprayNeutered.toEmoji()
        binding.specialNeeds.text = animalDetails.specialNeeds.toEmoji()
        binding.declawed.text = animalDetails.declawed.toEmoji()
        binding.shotsCurrent.text = animalDetails.shotsCurrent.toEmoji()

        val doubleTapGestureListener =
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    (binding.heartImage.drawable as Animatable?)?.start()
                    return true
                }

                override fun onDown(e: MotionEvent) = true
            }
        val doubleTapGestureDetector =
            GestureDetector(requireContext(), doubleTapGestureListener)

        binding.image.setOnTouchListener { v, event ->
            doubleTapGestureDetector.onTouchEvent(event)
        }

        // start both scaleX and scaleY animations
        callScaleXSpringAnimation.animateToFinalPosition(FLING_SCALE)
        callScaleYSpringAnimation.animateToFinalPosition(FLING_SCALE)

        val flingGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            // tell that the fling has been consumed by the view the listener is attached to
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                callFlingXAnimation.setStartVelocity(velocityX).start()
                callFlingYAnimation.setStartVelocity(velocityY).start()
                return true
            }

            // tell that the tap event has been consumed by the view the listener
            // is attached to
            override fun onDown(e: MotionEvent?): Boolean = true
        }

        val flingGestureDetector = GestureDetector(requireContext(), flingGestureListener)

        binding.call.setOnTouchListener { v, event ->
            flingGestureDetector.onTouchEvent(event)
        }

        callFlingYAnimation.addEndListener { _, _, _, _ ->
            if (areViewsOverlapping(binding.call, binding.image)) {
                val action = AnimalDetailsFragmentDirections.actionDetailsToSecret()
                findNavController().navigate(action)
            }
        }

        binding.adoptButton.setOnClickListener {
            binding.adoptButton.startLoading()
            viewModel.onEvent(AnimalDetailsEvent.AdoptAnimal)
        }
        if (adopted) {
            binding.adoptButton.done()
            binding.adoptButton.setOnClickListener(null)
        }
    }

    private fun displayError() {
        startAnimation(R.raw.lazy_cat)
        binding.scrollView.isVisible = false
        Snackbar.make(requireView(), R.string.an_error_occurred, Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun displayLoading() {
        startAnimation(R.raw.happy_dog)
        binding.scrollView.isVisible = false
    }

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
