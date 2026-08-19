package com.bakjoul.testwithings.di

import com.bakjoul.testwithings.ui.home.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

private val viewModelModule = module {
    factoryOf(::HomeViewModel)
}

val appModules = module {
    includes(viewModelModule)
}
