package com.expiryreminder.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expiryreminder.app.data.Category
import com.expiryreminder.app.data.CategoryDao
import com.expiryreminder.app.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val categoryDao = (application as com.expiryreminder.app.ExpiryApp).database.categoryDao()
    
    val parentCategories: Flow<List<Category>> = categoryDao.getParentCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun getChildCategories(parentId: Long): Flow<List<Category>> {
        return categoryDao.getChildCategories(parentId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.insertCategory(category)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
    
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.updateCategory(category)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
    
    fun deleteAllCategories() {
        viewModelScope.launch {
            categoryDao.deleteAllCategories()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
    
    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)
    }
    
    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)
    }

    suspend fun allCategoriesForSync(): List<Category> {
        return categoryDao.getAllCategoriesForSync()
    }

    fun insertCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.insertCategory(category)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }
}
