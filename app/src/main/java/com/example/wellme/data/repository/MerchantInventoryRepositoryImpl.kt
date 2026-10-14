package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.data.remote.model.MerchantItemDto
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.repository.MerchantInventoryRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantInventoryRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : MerchantInventoryRepository {

    private val TAG = "MerchantInventoryRepo"

    override fun getInventory(merchantId: String): Flow<List<MerchantItem>> = flow {
        Log.d(TAG, "Fetching inventory from items table for merchant: $merchantId")
        try {
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

            val finalDtos = if (dtos.isEmpty()) {
                postgrest["items"]
                    .select()
                    .decodeList<MerchantItemDto>()
            } else {
                dtos
            }

            val items = finalDtos.map { it.toDomain() }
            emit(items)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching items from Supabase items table for merchant $merchantId", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upsertItem(item: MerchantItem): Result<String> {
        return try {
            val itemId = postgrest.rpc(
                function = "merchant_upsert_item",
                parameters = kotlinx.serialization.json.buildJsonObject {
                    put("p_item_id", if (item.id.isBlank() || item.id == "0") null else item.id)
                    put("p_name", item.name)
                    put("p_price_kes", item.price)
                    put("p_stock_units", item.stockUnits)
                    put("p_category", item.category)
                    put("p_description", item.description)
                    put("p_image_url", item.imageUrl)
                }
            ).decodeAs<String>()
            Result.success(itemId)
        } catch (e: Exception) {
            Log.e(TAG, "Error in merchant_upsert_item RPC", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(itemId: String): Result<Boolean> {
        return try {
            val deleted = postgrest.rpc(
                function = "merchant_delete_item",
                parameters = kotlinx.serialization.json.buildJsonObject {
                    put("p_item_id", itemId)
                }
            ).decodeAs<Boolean>()
            Result.success(deleted)
        } catch (e: Exception) {
            Log.e(TAG, "Error in merchant_delete_item RPC", e)
            Result.failure(e)
        }
    }

    override suspend fun updateStock(itemId: String, newStock: Int): Result<Int> {
        return try {
            val stock = postgrest.rpc(
                function = "merchant_update_item_stock",
                parameters = kotlinx.serialization.json.buildJsonObject {
                    put("p_item_id", itemId)
                    put("p_stock_units", newStock)
                }
            ).decodeAs<Int>()
            Result.success(stock)
        } catch (e: Exception) {
            Log.e(TAG, "Error in merchant_update_item_stock RPC", e)
            Result.failure(e)
        }
    }
}
