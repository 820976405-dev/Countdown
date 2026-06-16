package com.expiryreminder.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: Long?,
    val imageUri: String?,
    val expireDate: Long,
    val purchaseDate: Long?,
    val quantity: Int,
    val unit: String,
    val location: String,
    val note: String,
    val remindDays: String,
    val createdAt: Long,
    val updatedAt: Long
)
