package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MaterialItem(
    val name: String,
    val requested: String = "",
    val received: String = "",
    val used: String = "",
    val remaining: String = "",
    val comment: String = ""
)

@Entity(tableName = "field_entries")
data class FieldEntry(
    @PrimaryKey val id: String,
    val checker: String,
    val tm: String,
    val block: String,
    val subBlock: String,
    val mrfNumber: String,
    val drrCode: String,
    val schemes: List<String>,
    val materials: List<MaterialItem>,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)
