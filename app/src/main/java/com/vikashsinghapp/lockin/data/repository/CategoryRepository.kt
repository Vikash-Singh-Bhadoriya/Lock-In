package com.vikashsinghapp.lockin.data.repository

import com.vikashsinghapp.lockin.data.dao.CategoryDao
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = dao.getAllCategories()
    suspend fun insertCategory(category: CategoryEntity): Long = dao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = dao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = dao.deleteCategory(category)
    suspend fun getCategoryById(id: Long): CategoryEntity? = dao.getCategoryById(id)
}

