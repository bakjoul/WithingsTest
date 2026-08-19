package com.bakjoul.testwithings.di

import com.bakjoul.testwithings.data.ImageRepositoryImpl
import com.bakjoul.testwithings.data.PixabayApi
import com.bakjoul.testwithings.domain.ImageRepository
import com.bakjoul.testwithings.domain.SearchForImagesUseCase
import com.bakjoul.testwithings.ui.home.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

private val networkModule = module {
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10_000
                connectTimeoutMillis = 5_000
            }
            defaultRequest {
                url("https://pixabay.com/api/")
            }
        }
    }
}

private val dataModule = module {
    single<PixabayApi> { PixabayApi(get()) }
}

private val viewModelModule = module {
    factoryOf(::HomeViewModel)
}

private val repositoryModule = module {
    single<ImageRepository> { ImageRepositoryImpl(get()) }
}

private val useCaseModule = module {
    factoryOf(::SearchForImagesUseCase)
}

val appModules = module {
    includes(networkModule, dataModule, viewModelModule, repositoryModule, useCaseModule)
}
