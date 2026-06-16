package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DraftData(
    val checker: String = "",
    val tm: String = "",
    val block: String = "",
    val subBlock: String = "",
    val mrfNumber: String = "",
    val drrCode: String = "",
    val otherScheme: String = "",
    val schemes: List<String> = emptyList(),
    val selectedMaterials: List<String> = emptyList(),
    val materialItems: List<MaterialItem> = emptyList()
)
