package com.vikashsinghapp.lockin.data.repository

import com.vikashsinghapp.lockin.data.dao.TemplateDao
import com.vikashsinghapp.lockin.data.entity.TemplateEntity
import com.vikashsinghapp.lockin.data.entity.TemplateTaskEntity
import com.vikashsinghapp.lockin.data.entity.TemplateWithTasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TemplateRepository @Inject constructor(
    private val templateDao: TemplateDao
) {
    fun getAllTemplatesWithTasks(): Flow<List<TemplateWithTasks>> {
        // Intercept the flow and sort the nested tasks for EVERY template
        return templateDao.getTemplatesWithTasks().map { templatesList ->
            templatesList.map { templateData ->
                // Sort the tasks by the orderIndex we saved
                templateData.copy(tasks = templateData.tasks.sortedBy { it.orderIndex })
            }
        }
    }

    fun getTemplateWithTasksById(templateId: Long): Flow<TemplateWithTasks?> {
        // Intercept the flow and sort the tasks for this specific template
        return templateDao.getTemplateWithTasksById(templateId).map { templateData ->
            // Use safe call (?.) because templateData might be null if it doesn't exist yet
            templateData?.copy(tasks = templateData.tasks.sortedBy { it.orderIndex })
        }
    }

    suspend fun createTemplate(name: String): Long {
        return templateDao.insertTemplate(TemplateEntity(name = name))
    }

    suspend fun deleteTemplate(template: TemplateEntity) {
        templateDao.deleteTemplate(template)
    }

    suspend fun updateTemplate(template: TemplateEntity) {
        templateDao.updateTemplate(template)
    }

    // Overwrites all tasks in a template with a new list (useful for the Template Editor)
    suspend fun saveTasksToTemplate(templateId: Long, tasks: List<TemplateTaskEntity>) {
        templateDao.deleteAllTasksForTemplate(templateId)
        templateDao.insertAllTemplateTasks(tasks)
    }
}