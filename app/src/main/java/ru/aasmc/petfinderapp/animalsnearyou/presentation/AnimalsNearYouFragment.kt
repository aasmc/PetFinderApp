package ru.aasmc.petfinderapp.animalsnearyou.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.common.presentation.AnimalClickListener
import ru.aasmc.petfinderapp.common.presentation.AnimalsAdapter
import ru.aasmc.petfinderapp.common.presentation.Event
import ru.aasmc.petfinderapp.databinding.FragmentAnimalsNearYouBinding

@AndroidEntryPoint
class AnimalsNearYouFragment : Fragment() {

    companion object {
        private const val ITEMS_PER_ROW = 2
    }

    private val binding get() = _binding!!

    private var _binding: FragmentAnimalsNearYouBinding? = null

    private val viewModel: AnimalsNearYouFragmentViewModel
            by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimalsNearYouBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        requestInitialAnimalsList()
    }

    private fun requestInitialAnimalsList() {
        viewModel.onEvent(AnimalsNearYouEvent.LoadAnimals)
    }

    /**
     * There's a good reason why the adapter value only exists in the scope of this method:
     * having an Adapter as a property of a Fragment is a known way of leaking the
     * RecyclerView. That's because when the View is destroyed, the RecyclerView is destroyed
     * along with it. But if the Fragment references the Adapter, the garbage collector
     * won't be able to collect the RecyclerView instance because Adapters and RecyclerViews
     * have a circular dependency - they reference each other.
     *
     * If there's a need for an Adapter as a property of a Fragment, then either:
     *  - null out the Adapter property in onDestroyView
     *  - Null out the Adapter reference in the RecyclerView itself before doing the
     *    same for the binding. in onDestroyView
     *    e.g.
     *
     *    override fun onDestroyView() {
     *      super.onDestroyView()
     *
     *      // either
     *      adapter = null
     *      // or
     *      binding.recyclerView.adapter = null
     *      _binding = null
     *    }
     */
    private fun setupUI() {
        val adapter = createAdapter()
        setupRecyclerView(adapter)
        subscribeToViewStateUpdates(adapter)
    }

    private fun createAdapter(): AnimalsAdapter {
        return AnimalsAdapter().apply {
            setOnAnimalClickListener(object: AnimalClickListener {
                override fun onClick(animalId: Long) {
                    val action = AnimalsNearYouFragmentDirections.actionAnimalsNearYouToDetails(animalId)
                    findNavController().navigate(action)
                }
            })
        }
    }

    private fun setupRecyclerView(animalsAdapter: AnimalsAdapter) {
        binding.animalsRecyclerView.apply {
            adapter = animalsAdapter
            layoutManager = GridLayoutManager(requireContext(), ITEMS_PER_ROW)
            setHasFixedSize(true)
            addOnScrollListener(createInfiniteScrollListener(layoutManager as GridLayoutManager))
        }
    }

    private fun createInfiniteScrollListener(
        layoutManager: GridLayoutManager
    ): RecyclerView.OnScrollListener = object : InfiniteScrollListener(
        layoutManager,
        AnimalsNearYouFragmentViewModel.UI_PAGE_SIZE
    ) {
        override fun loadMoreItems() {
            requestMoreAnimals()
        }

        override fun isLastPage(): Boolean = viewModel.isLastPage

        override fun isLoading(): Boolean = viewModel.isLoadingMoreAnimals
    }

    private fun requestMoreAnimals() {
        viewModel.onEvent(AnimalsNearYouEvent.LoadAnimals)
    }

    private fun subscribeToViewStateUpdates(adapter: AnimalsAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    updateScreenState(it, adapter)
                }
            }
        }
    }

    private fun updateScreenState(
        state: AnimalsNearYouViewState,
        adapter: AnimalsAdapter
    ) {
        binding.progressBar.isVisible = state.loading
        adapter.submitList(state.animals)
        handleNoMoreAnimalsNearby(state.noMoreAnimalsNearby)
        handleFailures(state.failure)
    }

    private fun handleNoMoreAnimalsNearby(
        noMoreAnimalsNearby: Boolean
    ) {
        // placeholder method to prompt the user to try a different distance or
        // postal code if there aren't any more animals nearby.
    }

    private fun handleFailures(failure: Event<Throwable>?) {
        val unhandledFailure = failure?.getContentIfNotHandled() ?: return
        val fallbackMessage = getString(R.string.an_error_occurred)
        val snackbarMessage = if (unhandledFailure.message.isNullOrEmpty()) {
            fallbackMessage
        } else {
            unhandledFailure.message!!
        }
        if (snackbarMessage.isNotEmpty()) {
            Snackbar.make(requireView(), snackbarMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Fragments can outlive their Views, so we need to clear up the binding
     * in onDestroyView.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}