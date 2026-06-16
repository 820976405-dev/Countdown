package com.expiryreminder.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY expireDate ASC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE expireDate <= :timestamp ORDER BY expireDate ASC")
    fun getExpiringItems(timestamp: Long): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): Item?

    @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY expireDate ASC")
    fun getItemsByCategory(categoryId: Long): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY expireDate ASC")
    fun searchItems(query: String): Flow<List<Item>>

    @Insert
    suspend fun insertItem(item: Item): Long

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    @Query("SELECT COUNT(*) FROM items")
    fun getItemCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM items WHERE expireDate <= :timestamp")
    fun getExpiringCount(timestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM items WHERE expireDate BETWEEN :now AND :timestamp")
    fun getExpiringSoonCount(now: Long, timestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM items WHERE expireDate < :timestamp")
    fun getExpiredCount(timestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM items WHERE expireDate > :timestamp")
    fun getNotExpiredCount(timestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM items WHERE expireDate > :now AND expireDate <= :timestamp")
    fun getExpiringSoonItemCount(now: Long, timestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM items WHERE expireDate > :timestamp")
    fun getSafeItemCount(timestamp: Long): Flow<Int>

    @Query("SELECT * FROM items ORDER BY expireDate ASC")
    suspend fun getAllItemsForWorker(): List<Item>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getTotalItemCount(): Int

    @Query("SELECT COUNT(*) FROM items WHERE expireDate >= :startOfDay AND expireDate < :endOfDay")
    suspend fun getExpiringSoonCountSync(startOfDay: Long, endOfDay: Long): Int
}
