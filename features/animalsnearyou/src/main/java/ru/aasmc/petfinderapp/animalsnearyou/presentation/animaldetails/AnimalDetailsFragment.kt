package ru.aasmc.petfinderapp.animalsnearyou.presentation.animaldetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.animalsnearyou.R
import ru.aasmc.petfinderapp.animalsnearyou.databinding.FragmentDetailsBinding
import ru.aasmc.petfinderapp.animalsnearyou.presentation.animaldetails.model.UIAnimalDetailed
import ru.aasmc.petfinderapp.common.utils.setImage
import ru.aasmc.petfinderapp.common.utils.toEnglish

@AndroidEntryPoint
class AnimalDetailsFragment : Fragment() {
    companion object {
        const val ANIMAL_ID = "id"
    }

    private var _binding: FragmentDetailsBinding? = null
    private val binding: FragmentDetailsBinding
        get() = _binding!!

    private val viewModel: AnimalDetailsFragmentViewModel by viewModels()
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

        addShareMenu()
        subscribeToStateUpdates()
        val event = AnimalDetailsEvent.LoadAnimalDetails(animalId!!)
        viewModel.handleEvent(event)
    }

    private fun addShareMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_share, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return if (menuItem.itemId == R.id.share) {
                    navigateToSharing()
                    true
                } else {
                    false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun navigateToSharing() {
        val animalId = requireArguments().getLong(ANIMAL_ID)
        val deepLink = NavDeepLinkRequest.Builder
            .fromUri("petsave://sharing/$animalId".toUri())
            .build()

        findNavController().navigate(deepLink)
    }

    private fun subscribeToStateUpdates() {
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

    private fun displayPetDetails(animalDetails: UIAnimalDetailed) {
        with(binding) {
            group.isVisible = true
            name.text = animalDetails.name
            description.text = animalDetails.description
            image.setImage(animalDetails.photo)
            sprayedNeutered.text = animalDetails.sprayNeutered.toEnglish()
            specialNeeds.text = animalDetails.specialNeeds.toEnglish()
        }
    }

    private fun displayError() {
        binding.group.isVisible = false
        Snackbar.make(
            requireView(),
            ru.aasmc.petfinderapp.common.R.string.an_error_occurred,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun displayLoading() {
        binding.group.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}