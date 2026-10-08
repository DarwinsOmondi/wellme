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
        Log.d(TAG, "Fetching real-time inventory from items table for merchant: $merchantId")
        try {
            // Strategy 1: Exact merchant_id match
            var dtos = postgrest["items"]
                .select {
                    filter {
                        eq("merchant_id", merchantId)
                    }
                }
                .decodeList<MerchantItemDto>()

            // Strategy 2: Case-insensitive ilike if exact eq returned empty
            if (dtos.isEmpty() && merchantId.isNotBlank()) {
                Log.d(TAG, "eq match returned 0 items, trying ilike for merchant_id: $merchantId")
                dtos = postgrest["items"]
                    .select {
                        filter {
                            ilike("merchant_id", merchantId)
                        }
                    }
                    .decodeList<MerchantItemDto>()
            }

            // Strategy 3: Query active items table if merchantId filter returns empty
            if (dtos.isEmpty()) {
                Log.d(TAG, "No merchant-specific items found, querying active items table")
                dtos = postgrest["items"]
                    .select()
                    .decodeList<MerchantItemDto>()
            }

            val items = dtos.map { it.toDomain() }
            Log.d(TAG, "Successfully retrieved ${items.size} real items from Supabase items table for $merchantId")
            emit(items)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching items from Supabase items table for merchant $merchantId", e)
            emit(emptyList())
        }

        // Real-time updates subscription
        try {
            val channel = realtime.channel("inventory_realtime_$merchantId")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "items"
            }.transform {
                try {
                    val freshDtos = postgrest["items"]
                        .select()
                        .decodeList<MerchantItemDto>()
                    emit(freshDtos.map { it.toDomain() })
                } catch (e: Exception) {
                    Log.e(TAG, "Error re-fetching items on realtime update", e)
                }
            }
            channel.subscribe()
            emitAll(changeFlow)
        } catch (e: Exception) {
            Log.w(TAG, "Realtime subscription failed for merchant $merchantId", e)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upsertItem(item: MerchantItem): Result<Unit> = try {
        Log.d(TAG, "Upserting item '${item.name}' for merchant '${item.merchantId}' into items table")
        postgrest["items"].upsert(MerchantItemDto.fromDomain(item))
        Log.d(TAG, "Item '${item.name}' successfully inserted into items table")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error upserting item '${item.name}' into items table", e)
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
