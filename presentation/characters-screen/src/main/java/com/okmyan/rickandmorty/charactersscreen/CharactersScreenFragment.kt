package com.okmyan.rickandmorty.charactersscreen

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.okmyan.rickandmorty.charactersscreen.adapters.CharacterAdapter
import com.okmyan.rickandmorty.charactersscreen.adapters.LoaderStateAdapter
import com.okmyan.rickandmorty.charactersscreen.databinding.FragmentCharactersScreenBinding
import com.okmyan.rickandmorty.charactersscreen.di.CharactersScreenComponentViewModel
import com.okmyan.rickandmorty.charactersscreen.layoutmanagers.SpeedyLinearLayoutManager
import com.okmyan.rickandmorty.domain.models.LifeStatus.Companion.EMPTY_VALUE
import com.skydoves.powerspinner.DefaultSpinnerAdapter
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.Lazy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class CharactersScreenFragment : Fragment(R.layout.fragment_characters_screen) {

    @Inject
    lateinit var characterAdapter: CharacterAdapter

    @Inject
    internal lateinit var charactersViewModelFactory: Lazy<CharactersViewModel.Factory>

    private val charactersViewModel: CharactersViewModel by viewModels {
        charactersViewModelFactory.get()
    }

    override fun onAttach(context: Context) {
        ViewModelProvider(this).get<CharactersScreenComponentViewModel>()
            .charactersComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCharactersScreenBinding.bind(view)

        val swipeRefreshLayout = binding.swipeRefreshContainer
        swipeRefreshLayout.setOnRefreshListener {
            onRefreshListener(swipeRefreshLayout)
        }

        val characters = binding.charactersList
        characters.layoutManager = SpeedyLinearLayoutManager(binding.root.context)
        characters.adapter = characterAdapter
            .withLoadStateFooter(
                footer = LoaderStateAdapter { characterAdapter.retry() }
            )

        charactersViewModel.charactersFlow.onEach {
            characterAdapter.submitData(it)
            characters.smoothScrollToPosition(0)
        }.launchIn(lifecycleScope)

        charactersViewModel.lifeStatusesFlow.onEach { statuses ->
            setLifeStatuses(binding.lifeStatusesSpinner, statuses)
        }.launchIn(lifecycleScope)

        binding.filters.setOnClickListener {
            characters.smoothScrollToPosition(0)
        }

        lifecycleScope.launch {
            val status = charactersViewModel.currentLifeStatusFlow
                .firstOrNull()
            status?.let {
                setTextToSpinner(binding.lifeStatusesSpinner, it)
            }
        }

    }

    private fun onRefreshListener(swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.isRefreshing = false
        characterAdapter.refresh()
    }

    private fun setLifeStatuses(spinner: PowerSpinnerView, statuses: List<String>) {
        spinner.apply {
            setSpinnerAdapter(DefaultSpinnerAdapter(this))
            setItems(statuses)
            setOnSpinnerItemSelectedListener<String> { _, oldItem, _, newItem ->
                setTextToSpinner(spinner, newItem)
                if (oldItem != newItem) {
                    charactersViewModel.setCurrentLifeStatus(newItem)
                }
            }
        }
    }

    private fun setTextToSpinner(spinner: PowerSpinnerView, status: String) {
        if (status == EMPTY_VALUE) {
            spinner.text = getString(R.string.life_statuses_spinner_title)
        } else {
            spinner.text = status
        }
    }

}
