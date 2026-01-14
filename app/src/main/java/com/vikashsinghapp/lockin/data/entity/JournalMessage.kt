package com.vikashsinghapp.lockin.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_messages")
data class JournalMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val content: String,
//    val type: MessageType,
    val timestamp: Long,
//    val category: Category = Category.Uncategorized,
    val duringPromiseTaskId: Long?, // nullable link to a Task id
)

//enum class MessageType {
//    TEXT,
//    IMAGE
//}
//
//enum class Category {
//    Achievement,
//    WhenDemotivated,
//    Reflection,
//    Uncategorized
//}