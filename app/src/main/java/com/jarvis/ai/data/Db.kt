package com.jarvis.ai.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isUser: Boolean,
    val text: String,
    val time: Long = System.currentTimeMillis()
)

@Entity(tableName = "prefs")
data class PrefEntity(@PrimaryKey val key: String, val value: String)

@Dao
interface JarvisDao {
    @Insert suspend fun addMessage(m: MessageEntity)
    @Query("SELECT * FROM messages ORDER BY id DESC LIMIT 50")
    fun recentMessages(): Flow<List<MessageEntity>>
    @Upsert suspend fun setPref(p: PrefEntity)
}

@Database(entities = [MessageEntity::class, PrefEntity::class], version = 1)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun dao(): JarvisDao
    companion object {
        fun build(ctx: Context): JarvisDatabase =
            Room.databaseBuilder(ctx, JarvisDatabase::class.java, "jarvis.db").build()
    }
}
