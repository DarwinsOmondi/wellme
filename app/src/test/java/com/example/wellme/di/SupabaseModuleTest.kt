package com.example.wellme.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.plugins.PluginManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SupabaseModuleTest {

    @Test
    fun `provideSupabaseAuth returns auth from client`() {
        val client: SupabaseClient = mock()
        val pluginManager: PluginManager = mock()
        val expected: Auth = mock()
        
        whenever(client.pluginManager).thenReturn(pluginManager)
        whenever(pluginManager.installedPlugins).thenReturn(mapOf(Auth.key to expected))
        
        val result = SupabaseModule.provideSupabaseAuth(client)
        assertEquals(expected, result)
    }

    @Test
    fun `provideSupabasePostgrest returns postgrest from client`() {
        val client: SupabaseClient = mock()
        val pluginManager: PluginManager = mock()
        val expected: Postgrest = mock()
        
        whenever(client.pluginManager).thenReturn(pluginManager)
        whenever(pluginManager.installedPlugins).thenReturn(mapOf(Postgrest.key to expected))
        
        val result = SupabaseModule.provideSupabasePostgrest(client)
        assertEquals(expected, result)
    }

    @Test
    fun `provideSupabaseStorage returns storage from client`() {
        val client: SupabaseClient = mock()
        val pluginManager: PluginManager = mock()
        val expected: Storage = mock()
        
        whenever(client.pluginManager).thenReturn(pluginManager)
        whenever(pluginManager.installedPlugins).thenReturn(mapOf(Storage.key to expected))
        
        val result = SupabaseModule.provideSupabaseStorage(client)
        assertEquals(expected, result)
    }

    @Test
    fun `provideSupabaseRealtime returns realtime from client`() {
        val client: SupabaseClient = mock()
        val pluginManager: PluginManager = mock()
        val expected: Realtime = mock()
        
        whenever(client.pluginManager).thenReturn(pluginManager)
        whenever(pluginManager.installedPlugins).thenReturn(mapOf(Realtime.key to expected))
        
        val result = SupabaseModule.provideSupabaseRealtime(client)
        assertEquals(expected, result)
    }
}
