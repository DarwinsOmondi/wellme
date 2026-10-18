package com.example.wellme.di

import com.codeskop.sdk.okhttp.CodeskopEventListener
import com.codeskop.sdk.okhttp.CodeskopOkHttpInterceptor
import com.example.wellme.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        android.util.Log.d("SupabaseModule", "Initializing Supabase with URL: ${BuildConfig.SUPABASE_URL}")
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY,
        ) {
            requestTimeout = 30.seconds
            httpEngine = OkHttp.create {
                addInterceptor(CodeskopOkHttpInterceptor())
                config {
                    connectTimeout(30, TimeUnit.SECONDS)
                    readTimeout(30, TimeUnit.SECONDS)
                    writeTimeout(30, TimeUnit.SECONDS)
                    eventListenerFactory(CodeskopEventListener.factory())
                }
            }
            install(Auth) {
                autoLoadFromStorage = false
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
            install(Functions)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth = client.auth

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    @Provides
    @Singleton
    fun provideSupabaseStorage(client: SupabaseClient): Storage = client.storage

    @Provides
    @Singleton
    fun provideSupabaseRealtime(client: SupabaseClient): Realtime = client.realtime

    @Provides
    @Singleton
    fun provideSupabaseFunctions(client: SupabaseClient): Functions = client.functions
}
