package com.vikashsinghapp.lockin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.vikashsinghapp.lockin.data.entity.TemplateEntity
import com.vikashsinghapp.lockin.data.entity.TemplateTaskEntity
import com.vikashsinghapp.lockin.data.entity.TemplateWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    // --- TEMPLATES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TemplateEntity)

    @Query("SELECT * FROM templates ORDER BY id ASC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    // --- TEMPLATE TASKS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateTask(task: TemplateTaskEntity): Long

    @Update
    suspend fun updateTemplateTask(task: TemplateTaskEntity)

    @Query("UPDATE template_tasks SET category = :newName WHERE category = :oldName")
    suspend fun updateCategoryNameInTemplates(oldName: String, newName: String)

    @Delete
    suspend fun deleteTemplateTask(task: TemplateTaskEntity)

    // Used when editing a template to clear old tasks and insert new ones
    @Query("DELETE FROM template_tasks WHERE planId = :templateId")
    suspend fun deleteAllTasksForTemplate(templateId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTemplateTasks(tasks: List<TemplateTaskEntity>)

    // --- COMBINED (The Magic Query) ---
    @Transaction
    @Query("SELECT * FROM templates")
    fun getTemplatesWithTasks(): Flow<List<TemplateWithTasks>>

    @Transaction
    @Query("SELECT * FROM templates WHERE id = :templateId")
    fun getTemplateWithTasksById(templateId: Long): Flow<TemplateWithTasks?>
}