package com.vikashsinghapp.lockin.data.repository

import com.vikashsinghapp.lockin.data.dao.CategoryDao
import com.vikashsinghapp.lockin.data.dao.PromiseDao
import com.vikashsinghapp.lockin.data.dao.TemplateDao
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val promiseTaskDao: PromiseDao,
    private val templateDao: TemplateDao,
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)

    // We pass the old name so we know which tasks to target!
    suspend fun updateCategory(oldName: String, updatedCategory: CategoryEntity) {
        // 1. Update the actual category in the DB
        categoryDao.updateCategory(updatedCategory)

        // 2. Cascade the name change to all live/historical tasks
        promiseTaskDao.updateCategoryNameInTasks(oldName = oldName, newName = updatedCategory.name)

        // 3. Cascade the name change to all saved templates
        templateDao.updateCategoryNameInTemplates(oldName = oldName, newName = updatedCategory.name)
    }

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)
    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)
}

