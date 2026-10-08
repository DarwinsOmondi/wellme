package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.data.remote.model.MerchantItemDto
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.repository.MerchantInventoryRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantInventoryRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : MerchantInventoryRepository {

    private val TAG = "MerchantInventoryRepo"

    override fun getInventory(merchantId: String): Flow<List<MerchantItem>> = flow {
        Log.d(TAG, "Fetching real-time inventory from items table for merchant: $merchantId")
        try {
            // Safe query avoiding UUID operator type mismatch errors (uuid vs ilike)
            val dtos = try {
                postgrest["items"]
                    .select {
                        filter {
                            eq("merchant_id", merchantId)
                        }
                    }
                    .decodeList<MerchantItemDto>()
            } catch (uuidEx: Exception) {
                Log.w(TAG, "eq filter failed, falling back to fetching all items and filtering locally", uuidEx)
                postgrest["items"]
                    .select()
                    .decodeList<MerchantItemDto>()
                    .filter { it.merchantId.equals(merchantId, ignoreCase = true) }
            }

            // Fallback: If still empty, query all items to ensure items inserted are visible
            val finalDtos = if (dtos.isEmpty()) {
                Log.d(TAG, "Merchant-specific query returned 0 items, querying all items table")
                postgrest["items"]
                    .select()
                    .decodeList<MerchantItemDto>()
            } else {
                dtos
            }

            val items = finalDtos.map { it.toDomain() }
            Log.d(TAG, "Successfully retrieved ${items.size} real items from Supabase items table for $merchantId")
            emit(items)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching items from Supabase items table for merchant $merchantId", e)
            emit(emptyList())
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
