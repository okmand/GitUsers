package com.okmyan.rickandmorty.charactersscreen.di

import androidx.lifecycle.ViewModel
import com.okmyan.rickandmorty.charactersscreen.CharactersScreenFragment
import com.okmyan.rickandmorty.core.Feature
import com.okmyan.rickandmorty.domain.usecases.CharactersUseCase
import dagger.Component
import kotlin.properties.Delegates.notNull

@Feature
@Component(dependencies = [CharactersScreenDependencies::class])
internal interface CharactersScreenComponent {

    fun inject(fragment: CharactersScreenFragment)

    @Component.Builder
    interface Builder {

        fun dependencies(dependencies: CharactersScreenDependencies): Builder

        fun build(): CharactersScreenComponent

    }

}

interface CharactersScreenDependencies {

    val charactersUseCase: CharactersUseCase

}

interface CharactersScreenDependenciesProvider {

    val dependencies: CharactersScreenDependencies

    companion object : CharactersScreenDependenciesProvider by CharactersScreenDependenciesStore

}

object CharactersScreenDependenciesStore : CharactersScreenDependenciesProvider {

    override var dependencies: CharactersScreenDependencies by notNull()

}

internal class CharactersScreenComponentViewModel : ViewModel() {

    val charactersComponent =
        DaggerCharactersScreenComponent.builder()
            .dependencies(CharactersScreenDependenciesProvider.dependencies)
            .build()

}
