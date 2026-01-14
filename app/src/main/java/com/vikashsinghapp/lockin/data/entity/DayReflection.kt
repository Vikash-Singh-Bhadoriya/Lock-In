package com.vikashsinghapp.lockin.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_reflections")
data class DayReflection(

    @PrimaryKey val dayStartMillis: Long,   // WHAT day this reflection belongs to => 11:58 PM (same day)
    val createdAtMillis: Long,            // WHEN the reflection was written => 12:15 AM (next day, but reflecting on yesterday)
    val summarySentence: String,

    val plannedCount: Int,
    val completedCount: Int,
    val imperfectCount: Int,
    val brokenCount: Int,
)

/*
* Below are small, focused additions and fixes: a `DayReflection` entity and DAO, an updated `LockInDatabase`, a lightweight repository (`LockInRepository`) that wraps DAOs, a `JournalViewModel` exposing journal messages and insert API, and a minimal end\-to\-end `JournalScreen` Compose UI that lets the user view and add journal messages.

`app/src/main/java/com/vikashsinghapp/lockin/data/DayReflection.kt` — simple entity for end\-of\-day reflections:
```kotlin
```kotlin
package com.vikashsinghapp.lockin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_reflections")
data class DayReflection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dayStartMillis: Long, // canonical day identifier (local midnight millis)
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
```
```

`app/src/main/java/com/vikashsinghapp/lockin/data/DayReflectionDao.kt` — CRUD surface for reflections:
```kotlin
```kotlin
package com.vikashsinghapp.lockin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DayReflectionDao {
    @Insert
    suspend fun insert(reflection: DayReflection): Long

    @Update
    suspend fun update(reflection: DayReflection)

    @Query("SELECT * FROM day_reflections WHERE dayStartMillis = :dayStart LIMIT 1")
    fun getForDay(dayStart: Long): Flow<DayReflection?>
}
```
```

`app/src/main/java/com/vikashsinghapp/lockin/data/LockInDatabase.kt` — uncomment DAO and keep entities list consistent:
```kotlin
```kotlin
package com.vikashsinghapp.lockin.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Promise::class, JournalMessage::class, DayReflection::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class LockInDatabase : RoomDatabase() {
    abstract fun promiseDao(): PromiseDao
    abstract fun journalMessageDao(): JournalMessageDao
    abstract fun dayReflectionDao(): DayReflectionDao
}
```
```

`app/src/main/java/com/vikashsinghapp/lockin/data/LockInRepository.kt` — repository wrapping DAOs; keeps UI and business logic decoupled:
```kotlin
```kotlin
package com.vikashsinghapp.lockin.data

import kotlinx.coroutines.flow.Flow

class LockInRepository(
    private val db: LockInDatabase
) {

    // Journal
    fun getAllJournalMessages(): Flow<List<JournalMessage>> = db.journalMessageDao().getAll()
    fun getJournalForTask(taskId: Long): Flow<List<JournalMessage>> = db.journalMessageDao().getForTask(taskId)
    suspend fun insertJournalMessage(message: JournalMessage): Long = db.journalMessageDao().insert(message)
    suspend fun updateJournalMessage(message: JournalMessage) = db.journalMessageDao().update(message)
    suspend fun deleteJournalMessage(message: JournalMessage) = db.journalMessageDao().delete(message)

    // Day reflections
    suspend fun insertReflection(reflection: DayReflection): Long = db.dayReflectionDao().insert(reflection)
    fun getReflectionForDay(dayStart: Long): Flow<DayReflection?> = db.dayReflectionDao().getForDay(dayStart)

    // Expose Promise DAO for other features
    val promiseDao: PromiseDao get() = db.promiseDao()
}
```
```

`app/src/main/java/com/vikashsinghapp/lockin/journal/JournalViewModel.kt` — ViewModel that collects journal flow and provides add API:
```kotlin
```kotlin
package com.vikashsinghapp.lockin.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.LockInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class JournalViewModel(
    private val repository: LockInRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _messages = MutableStateFlow<List<JournalMessage>>(emptyList())
    val messages: StateFlow<List<JournalMessage>> = _messages

    init {
        repository.getAllJournalMessages()
            .onEach { _messages.value = it }
            .launchIn(viewModelScope)
    }

    fun addMessage(content: String, duringTaskId: Long? = null) {
        if (content.isBlank()) return
        val msg = JournalMessage(
            content = content.trim(),
            timestamp = clock(),
            duringTaskId = duringTaskId
        )
        viewModelScope.launch {
            repository.insertJournalMessage(msg)
        }
    }

    fun deleteMessage(message: JournalMessage) {
        viewModelScope.launch {
            repository.deleteJournalMessage(message)
        }
    }
}
```
```

`app/src/main/java/com/vikashsinghapp/lockin/ui/JournalScreen.kt` — minimal sober Compose screen (list + input + save). Pass `viewModel` from activity/DI; UI is UI\-agnostic and simple:
```kotlin
```kotlin
package com.vikashsinghapp.lockin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.journal.JournalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(viewModel: JournalViewModel, modifier: Modifier = Modifier) {
    val messages by viewModel.messages
    var input by remember { mutableStateOf("") }
    var taskIdInput by remember { mutableStateOf("") }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Journal", modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
            items(messages) { msg ->
                JournalRow(msg = msg, formattedTime = sdf.format(Date(msg.timestamp))) {
                    // optional: single tap to delete (UI choice; keep friction low)
                    viewModel.deleteMessage(msg)
                }
            }
        }

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Write something (no emojis)") }
        )

        OutlinedTextField(
            value = taskIdInput,
            onValueChange = { taskIdInput = it.filter { ch -> ch.isDigit() } },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            label = { Text("Optional task id") }
        )

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Button(onClick = {
                val tid = taskIdInput.takeIf { it.isNotBlank() }?.toLongOrNull()
                viewModel.addMessage(input, tid)
                input = ""
                taskIdInput = ""
            }) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun JournalRow(msg: JournalMessage, formattedTime: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(text = formattedTime, modifier = Modifier.padding(bottom = 4.dp))
        Text(text = msg.content)
        if (msg.duringTaskId != null) {
            Text(text = "During task: ${msg.duringTaskId}", modifier = Modifier.padding(top = 4.dp))
        }
    }
}
```
```

Notes:
- `LockInDatabase` was updated to expose `dayReflectionDao()`. Ensure you rebuild Room-generated code after adding the DAO/entity.
- `JournalScreen` uses a direct `JournalViewModel` instance; integrate with Hilt/ViewModelProvider in your app code (factory or Hilt binding).
- The repository keeps methods minimal and UI-agnostic; extend only if needed.
- No UX dopamine elements included; Delete on tap is simple and reversible by design if you prefer to remove it.*/