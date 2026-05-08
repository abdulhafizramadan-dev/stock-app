package com.ahr.stock

import android.app.Application
import com.ahr.stock.di.dataModule
import com.ahr.stock.di.domainModule
import com.ahr.stock.di.networkModule
import com.ahr.stock.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(networkModule, dataModule, domainModule, presentationModule)
        }
    }
}

