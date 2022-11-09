package ru.aasmc.petfinderapp.search.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.IdRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.common.presentation.AnimalClickListener
import ru.aasmc.petfinderapp.common.presentation.AnimalsAdapter
import ru.aasmc.petfinderapp.common.presentation.Event
import ru.aasmc.petfinderapp.databinding.FragmentSearchBinding
import ru.aasmc.petfinderapp.search.domain.usecases.GetSearchFilters

/**
 * Fragment responsible for the following logic:
 *  - the user types the animal's name in the search query
 *  - the user can filter the queries by the age and type of animal
 *  - the app searches the cache for matching animals
 *  - if no animals exist locally, the app sends a request to the API
 *  - the app stores the API result, it finds one, and shows the search result to the user.
 */
@AndroidEntryPoint
class SearchFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentSearchBinding? = null

    private val viewModel: SearchFragmentViewModel by viewModels()

    companion object {
        private const val ITEMS_PER_ROW = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        prepareForSearch()
    }

    private fun setupUI() {
        val adapter = createAdapter()
        setupRecyclerView(adapter)
        subscribeToViewStateUpdates(adapter)
    }

    private fun createAdapter(): AnimalsAdapter {
        return AnimalsAdapter().apply {
            setOnAnimalClickListener(object : AnimalClickListener {
                override fun onClick(animalId: Long) {
                    val action = SearchFragmentDirections.actionSearchToDetails(animalId)
                    findNavController().navigate(action)
                }
            })
        }
    }

    private fun setupRecyclerView(searchAdapter: AnimalsAdapter) {
        binding.searchRecyclerView.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(requireContext(), ITEMS_PER_ROW)
            setHasFixedSize(true)
        }
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
        newState: SearchViewState,
        searchAdapter: AnimalsAdapter
    ) {
        val (
            initialState,
            searchResults,
            ageFilterValues,
            typeFilterValues,
            searchingRemotely,
            noResultsState,
            failure
        ) = newState

        updateInitialStateViews(initialState)
        searchAdapter.submitList(searchResults)
        setupMenuValues(ageFilterValues.getContentIfNotHandled(), R.id.age_dropdown)
        setupMenuValues(typeFilterValues.getContentIfNotHandled(), R.id.type_dropdown)
        updateRemoteSearchViews(searchingRemotely)
        updateNoResultsViews(noResultsState)
        handleFailures(failure)
    }

    private fun updateNoResultsViews(noResultsState: Boolean) {
        binding.noSearchResultsImageView.isVisible = noResultsState
        binding.noSearchResultsText.isVisible = noResultsState
    }

    private fun updateRemoteSearchViews(searchingRemotely: Boolean) {
        binding.searchRemotelyProgressBar.isVisible =
            searchingRemotely
        binding.searchRemotelyText.isVisible = searchingRemotely
    }

    private fun setupMenuValues(menuValues: List<String>?, @IdRes dropdownId: Int) {
        if (menuValues == null || menuValues.isEmpty()) return

        val dropdown: AutoCompleteTextView =
            binding.collapsibleSearchParamsContainer.findViewById(dropdownId)

        setupValuesFor(dropdown, menuValues)
    }

    private fun setupValuesFor(
        dropdown: AutoCompleteTextView,
        dropdownValues: List<String>
    ) {
        dropdown.setAdapter(createMenuAdapter(dropdownValues))
        dropdown.setText(dropdownValues.first(), false)
    }

    private fun createMenuAdapter(adapterValues: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_popup_item,
            adapterValues
        )
    }

    private fun updateInitialStateViews(initialState: Boolean) {
        binding.initialSearchImageView.isVisible = initialState
        binding.initialSearchText.isVisible = initialState
    }

    private fun handleFailures(failure: Event<Throwable>?) {
        val unhandledFailure = failure?.getContentIfNotHandled() ?: return
        handleThrowable(unhandledFailure)
    }

    private fun handleThrowable(exception: Throwable) {
        val fallbackMessage = getString(R.string.an_error_occurred)
        val snackbarMessage = if (exception.message.isNullOrEmpty()) {
            fallbackMessage
        } else {
            exception.message!!
        }
        if (snackbarMessage.isNotEmpty()) {
            Snackbar.make(requireView(), snackbarMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun prepareForSearch() {
        setupDropdownMenuListeners()
        setupSearchViewListener()
        viewModel.onEvent(SearchEvent.PrepareForSearch)
    }

    private fun setupSearchViewListener() {
        val searchView: SearchView =
            binding.collapsibleSearchParamsContainer.findViewById(R.id.search)

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.onEvent(
                        SearchEvent.QueryInput(query.orEmpty())
                    )
                    // this will hide the soft keyboard
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.onEvent(
                        SearchEvent.QueryInput(newText.orEmpty())
                    )
                    return true
                }
            }
        )
    }

    private fun setupDropdownMenuListeners() {
        setupDropdownMenuListenerFor(R.id.age_dropdown) { item ->
            viewModel.onEvent(SearchEvent.AgeValueSelected(item))
        }

        setupDropdownMenuListenerFor(R.id.type_dropdown) { item ->
            viewModel.onEvent(SearchEvent.TypeValueSelected(item))
        }
    }

    private fun setupDropdownMenuListenerFor(
        @IdRes dropdownMenu: Int,
        block: (item: String) -> Unit
    ) {
        val dropdown: AutoCompleteTextView =
            binding.collapsibleSearchParamsContainer.findViewById(dropdownMenu)
        dropdown.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                parent?.let {
                    block(it.adapter.getItem(position) as String)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}