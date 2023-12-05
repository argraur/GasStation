package dev.argraur.gasstation

import dev.argraur.gasstation.ui.App
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.argraur.gasstation.di.DomainModule
import dev.argraur.gasstation.di.NetworkModule
import dev.argraur.gasstation.di.UiModule
import org.koin.compose.KoinContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

fun main() {
    startKoin {
        modules(
            UiModule().module,
            NetworkModule().module,
            DomainModule().module
        )
    }
    application {
        KoinContext {
            Window(onCloseRequest = ::exitApplication) {
                App()
            }
        }
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}