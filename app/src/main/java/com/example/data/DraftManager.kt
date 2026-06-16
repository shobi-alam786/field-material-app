package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class DraftManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("field_material_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(DraftData::class.java)

    fun saveDraft(draft: DraftData) {
        try {
            val json = adapter.toJson(draft)
            prefs.edit().putString("draft_json", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDraft(): DraftData? {
        val json = prefs.getString("draft_json", null) ?: return null
        return try {
            adapter.fromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearDraft() {
        prefs.edit().remove("draft_json").apply()
    }

    fun saveLastChecker(checker: String) {
        prefs.edit().putString("last_checker", checker).apply()
    }

    fun getLastChecker(): String {
        return prefs.getString("last_checker", "") ?: ""
    }

    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", false)
    }
}
