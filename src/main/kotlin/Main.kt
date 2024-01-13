import di.mainModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

fun main() {
    startKoin { modules(mainModule) }
    runBlocking { Application().run() }
}