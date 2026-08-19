package com.bakjoul.testwithings.di

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

private val viewModelModule = module {
    factoryOf(::HomeViewModel)
}

val appModules = module {
    includes(networkModule, viewModelModule)
}
