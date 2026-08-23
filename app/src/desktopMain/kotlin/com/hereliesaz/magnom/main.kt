package com.hereliesaz.magnom

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hereliesaz.magnom.di.appModule
import com.hereliesaz.magnom.di.desktopModule
import org.koin.core.context.startKoin

fun main() {
    startKoin { modules(appModule, desktopModule) }
    application {
        Window(onCloseRequest = ::exitApplication, title = "MagNom") {
            App()
        }
    }
}
