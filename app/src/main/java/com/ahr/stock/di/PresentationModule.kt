package com.ahr.stock.di

import com.ahr.stock.presentation.screen.detail.DetailViewModel
import com.ahr.stock.presentation.screen.home.HomeViewModel
import com.ahr.stock.presentation.screen.sectorstocks.SectorStocksViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { DetailViewModel(get(), get(), get()) }
    viewModel { SectorStocksViewModel(get()) }
}

