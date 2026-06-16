package com.expiryreminder.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.data.ItemDao
import com.expiryreminder.app.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val itemDao = (application as com.expiryreminder.app.ExpiryApp).database.itemDao()
    
    val allItems: Flow<List<Item>> = itemDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val itemCount: Flow<Int> = itemDao.getItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    // 统一使用当天零点作为基准，因为 DatePicker 存储的 expireDate 是零点时间戳
    private val startOfDay: Long by lazy {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    private val sevenDaysFromNow = startOfDay + TimeUnit.DAYS.toMillis(7)

    val expiringItems: Flow<List<Item>> = itemDao.getExpiringItems(sevenDaysFromNow)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiringCount: Flow<Int> = itemDao.getExpiringSoonCount(startOfDay, sevenDaysFromNow)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiredCount: Flow<Int> = itemDao.getExpiredCount(startOfDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notExpiredCount: Flow<Int> = itemDao.getNotExpiredCount(startOfDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 详情进度条用：>7天的安全物品
    val safeItemCount: Flow<Int> = itemDao.getSafeItemCount(sevenDaysFromNow)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    fun addItem(item: Item) {
        viewModelScope.launch {
            itemDao.insertItem(item)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.updateItem(item)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
    
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.deleteItem(item)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
    
    fun deleteAllItems() {
        viewModelScope.launch {
            itemDao.deleteAllItems()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
    
    suspend fun getItemById(id: Long): Item? {
        return itemDao.getItemById(id)
    }
    
    fun searchItems(query: String): Flow<List<Item>> {
        return itemDao.searchItems(query)
    }

    suspend fun allItemsForSync(): List<Item> {
        return itemDao.getAllItemsForWorker()
    }
}
