package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.data.remote.model.MerchantItemDto
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.repository.MerchantInventoryRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantInventoryRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) : MerchantInventoryRepository {

    private val TAG = "MerchantInventoryRepo"

    override fun getInventory(merchantId: String): Flow<List<MerchantItem>> = flow {
        Log.d(TAG, "Fetching inventory for merchant: $merchantId")
        try {
            // Initial fetch
            val initialItems = postgrest["items"]
                .select { filter { eq("merchant_id", merchantId) } }
                .decodeList<MerchantItemDto>()
                .map { it.toDomain() }
            
            Log.d(TAG, "Successfully fetched ${initialItems.size} items for merchant $merchantId")
            emit(initialItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching initial inventory for $merchantId", e)
            emit(emptyList())
        }

        // Real-time updates with robust exception catching so it never crashes inventory flow
        try {
            Log.d(TAG, "Subscribing to real-time updates for merchant: $merchantId")
            val channel = realtime.channel("inventory_$merchantId")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "items"
            }.transform { _ ->
                try {
                    val currentItems = postgrest["items"]
                        .select { filter { eq("merchant_id", merchantId) } }
                        .decodeList<MerchantItemDto>()
                        .map { it.toDomain() }
                    emit(currentItems)
                } catch (e: Exception) {
                    Log.e(TAG, "Error re-fetching inventory on update", e)
                }
            }

            channel.subscribe()
            emitAll(changeFlow)
        } catch (e: Exception) {
            Log.w(TAG, "Realtime updates unavailable or failed to subscribe for merchant $merchantId", e)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upsertItem(item: MerchantItem): Result<Unit> = try {
        postgrest["items"].upsert(MerchantItemDto.fromDomain(item))
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error upserting item", e)
        Result.failure(e)
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> = try {
        postgrest["items"].delete { filter { eq("id", itemId) } }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting item", e)
        Result.failure(e)
    }

    override suspend fun updateStock(itemId: String, newStock: Int): Result<Unit> = try {
        postgrest["items"].update(mapOf("stock_units" to newStock)) {
            filter { eq("id", itemId) }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating stock", e)
        Result.failure(e)
    }
}
