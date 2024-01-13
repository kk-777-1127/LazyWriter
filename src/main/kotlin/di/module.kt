package di

import NotionClient
import NotionClientImpl
import ProcessorImpl
import QiitaClient
import QiitaClientImpl
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import io.github.kk777.OpenAiAppClient
import io.github.kk777.OpenAiAppClientImpl
import io.github.kk777.OpenAiClientCoordinator
import io.github.kk777.OpenAiClientCoordinatorImpl
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import processor.Processor
import kotlin.time.Duration.Companion.seconds

/*
TODO move modules to each layer
 */
val openAIModule = module {
    single { OpenAI(token = System.getenv("OPENAI_API_KEY"), timeout = Timeout(socket = 30.seconds),) }
    single<OpenAiAppClient> { OpenAiAppClientImpl(get()) }
    single<OpenAiClientCoordinator> { OpenAiClientCoordinatorImpl(client = get()) }
}

val notionModule = module {
    single<HttpClient>(named("notionHttpClient")) {
        HttpClient {
            defaultRequest {
                header("Authorization", "Bearer ${System.getenv("NOTION_API_KEY")}")
                header("Notion-Version", "2022-02-22")
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                filter { request ->
                    request.url.host.contains("ktor.io")
                }
            }
            install(ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }
        }
    }
    single<NotionClient> { NotionClientImpl(ktorClient = get(named("notionHttpClient"))) }
}

val qiitaModule = module {
    single<HttpClient>(named("qiitaHttpClient")) {
        HttpClient {
            defaultRequest {
                header("Authorization", "Bearer ${System.getenv("QIITA_API_KEY_")}")
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                filter { request ->
                    request.url.host.contains("ktor.io")
                }
            }
            install(ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }
        }
    }
    single<QiitaClient> { QiitaClientImpl(ktorClient = get(named("qiitaHttpClient"))) }
}

val processorModule = module {
    includes(openAIModule, notionModule, qiitaModule)
    single<Processor> { ProcessorImpl(get(), get(), get()) }
}

val mainModule = module {
    includes(processorModule)
}