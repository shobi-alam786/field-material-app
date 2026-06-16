package com.example.data

import androidx.room.Database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

class Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromMaterialList(value: List<MaterialItem>?): String {
        if (value == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, MaterialItem::class.java)
        return moshi.adapter<List<MaterialItem>>(type).toJson(value)
    }

    @TypeConverter
    fun toMaterialList(value: String): List<MaterialItem> {
        val type = Types.newParameterizedType(List::class.java, MaterialItem::class.java)
        return moshi.adapter<List<MaterialItem>>(type).fromJson(value) ?: emptyList()
    }
}

@Dao
interface FieldEntryDao {
    @Query("SELECT * FROM field_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<FieldEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FieldEntry)

    @Query("DELETE FROM field_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM field_entries")
    suspend fun deleteAllEntries()
}

@Database(entities = [FieldEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FieldDatabase : RoomDatabase() {
    abstract fun dao(): FieldEntryDao
}
