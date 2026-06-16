package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MaterialFields(
    val requested: String = "",
    val received: String = "",
    val used: String = "",
    val remaining: String = "",
    val comment: String = "",
    val isEdited: Boolean = false
)

class FieldViewModel(application: Application) : AndroidViewModel(application) {
    
    // Services
    private val db = Room.databaseBuilder(
        application,
        FieldDatabase::class.java,
        "field_entries_db"
    ).fallbackToDestructiveMigration().build()
    
    private val dao = db.dao()
    private val draftManager = DraftManager(application)

    // Saved entries
    val savedEntries: StateFlow<List<FieldEntry>> = dao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form inputs
    var checker = MutableStateFlow("")
        private set
    var tm = MutableStateFlow("")
        private set
    var block = MutableStateFlow("")
        private set
    var subBlock = MutableStateFlow("")
        private set
    var mrfNumber = MutableStateFlow("")
        private set
    var drrCode = MutableStateFlow("")
        private set
    var otherScheme = MutableStateFlow("")
        private set
    var selectedSchemes = MutableStateFlow<Set<String>>(emptySet())
        private set
    var selectedMaterials = MutableStateFlow<Set<String>>(emptySet())
        private set
    var customMaterials = MutableStateFlow<List<String>>(emptyList())
        private set

    // Material names mapping to their form states
    var materialFormState = MutableStateFlow<Map<String, MaterialFields>>(emptyMap())
        private set

    // UI Navigation & Preferences
    var currentTab = MutableStateFlow(0) // 0: New Entry, 1: Saved Entries
    var isDarkMode = MutableStateFlow(draftManager.isDarkMode())
        private set
    var searchQuery = MutableStateFlow("")

    init {
        // Load default checker
        checker.value = draftManager.getLastChecker()
        
        // Restore Draft if any exists
        val draft = draftManager.getDraft()
        if (draft != null) {
            checker.value = draft.checker
            tm.value = draft.tm
            block.value = draft.block
            subBlock.value = draft.subBlock
            mrfNumber.value = draft.mrfNumber
            drrCode.value = draft.drrCode
            otherScheme.value = draft.otherScheme
            selectedSchemes.value = draft.schemes.toSet()
            selectedMaterials.value = draft.selectedMaterials.toSet()
            
            // Re-populate custom materials
            val preset = AppConstants.presetMaterials.toSet()
            val customs = draft.selectedMaterials.filter { it !in preset }
            customMaterials.value = customs
            
            // Re-populate form state
            val map = draft.materialItems.associate { mat ->
                mat.name to MaterialFields(
                    requested = mat.requested,
                    received = mat.received,
                    used = mat.used,
                    remaining = mat.remaining,
                    comment = mat.comment,
                    isEdited = true
                )
            }
            materialFormState.value = map
        }
    }

    fun toggleDarkMode() {
        val newMode = !isDarkMode.value
        isDarkMode.value = newMode
        draftManager.saveDarkMode(newMode)
    }

    // Setters & Auto updates
    fun setChecker(value: String) {
        checker.value = value
        draftManager.saveLastChecker(value)
        triggerAutosave()
    }

    fun setTm(value: String) {
        tm.value = value
        // Auto-select block
        val targetBlock = AppConstants.tmToBlock[value] ?: ""
        block.value = targetBlock
        // Clear sub-block
        subBlock.value = ""
        // Auto-set DRR prefix code
        val prefix = AppConstants.tmToDrrPrefix[value] ?: ""
        drrCode.value = prefix
        triggerAutosave()
    }

    fun setBlock(value: String) {
        block.value = value
        subBlock.value = ""
        drrCode.value = ""
        triggerAutosave()
    }

    fun setSubBlock(value: String) {
        subBlock.value = value
        // Auto generate/append suffix to DRR prefix if any TM selected
        val prefix = AppConstants.tmToDrrPrefix[tm.value]
        if (prefix != null && drrCode.value.startsWith(prefix)) {
            // Keep it
        } else {
            drrCode.value = prefix ?: ""
        }
        triggerAutosave()
    }

    fun setMrfNumber(value: String) {
        mrfNumber.value = value
        triggerAutosave()
    }

    fun setDrrCode(value: String) {
        // format helper: format "C013-X-DDDDDD"
        var formatted = value.uppercase()
        val currentPrefix = AppConstants.tmToDrrPrefix[tm.value] ?: ""
        if (currentPrefix.isNotEmpty() && formatted.startsWith(currentPrefix)) {
            val suffix = formatted.substring(currentPrefix.length).replace("[^0-9]".toRegex(), "")
            formatted = if (suffix.length > 6) {
                currentPrefix + suffix.substring(0, 6) + "-" + suffix.substring(6)
            } else {
                currentPrefix + suffix
            }
        }
        drrCode.value = formatted
        triggerAutosave()
    }

    fun setOtherScheme(value: String) {
        otherScheme.value = value
        triggerAutosave()
    }

    fun toggleScheme(scheme: String) {
        val current = selectedSchemes.value.toMutableSet()
        if (current.contains(scheme)) {
            current.remove(scheme)
        } else {
            current.add(scheme)
        }
        selectedSchemes.value = current
        triggerAutosave()
    }

    fun addCustomMaterial(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        val lowercaseList = (AppConstants.presetMaterials + customMaterials.value).map { it.lowercase() }
        if (trimmed.lowercase() in lowercaseList) return false
        
        customMaterials.value = customMaterials.value + trimmed
        toggleMaterialSelected(trimmed, true)
        return true
    }

    fun toggleMaterialSelected(name: String, selected: Boolean) {
        val current = selectedMaterials.value.toMutableSet()
        if (selected) {
            current.add(name)
            // Initialize forms map
            if (!materialFormState.value.containsKey(name)) {
                val updatedMap = materialFormState.value.toMutableMap()
                updatedMap[name] = MaterialFields()
                materialFormState.value = updatedMap
            }
        } else {
            current.remove(name)
            val updatedMap = materialFormState.value.toMutableMap()
            updatedMap.remove(name)
            materialFormState.value = updatedMap
        }
        selectedMaterials.value = current
        triggerAutosave()
    }

    fun updateMaterialField(
        name: String,
        requested: String? = null,
        received: String? = null,
        used: String? = null,
        comment: String? = null
    ) {
        val currentForm = materialFormState.value[name] ?: MaterialFields()
        val newReq = requested ?: currentForm.requested
        val newRec = received ?: currentForm.received
        val newUsed = used ?: currentForm.used
        val newComment = comment ?: currentForm.comment

        // calculate remaining
        val recFloat = newRec.toFloatOrNull() ?: 0f
        val usedFloat = newUsed.toFloatOrNull() ?: 0f
        val remainingVal = if (newRec.isEmpty() && newUsed.isEmpty()) {
            ""
        } else if (usedFloat > recFloat) {
            val diff = recFloat - usedFloat
            if (diff % 1f == 0f) diff.toInt().toString() else "%.2f".format(Locale.US, diff)
        } else {
            val rem = recFloat - usedFloat
            if (rem % 1f == 0f) rem.toInt().toString() else "%.2f".format(Locale.US, rem)
        }

        val updatedMap = materialFormState.value.toMutableMap()
        updatedMap[name] = MaterialFields(
            requested = newReq,
            received = newRec,
            used = newUsed,
            remaining = remainingVal,
            comment = newComment,
            isEdited = true
        )
        materialFormState.value = updatedMap
        triggerAutosave()
    }

    fun clearForm() {
        // preserve checker
        val savedChecker = checker.value
        tm.value = ""
        block.value = ""
        subBlock.value = ""
        mrfNumber.value = ""
        drrCode.value = ""
        otherScheme.value = ""
        selectedSchemes.value = emptySet()
        selectedMaterials.value = emptySet()
        materialFormState.value = emptyMap()
        customMaterials.value = emptyList()
        draftManager.clearDraft()
    }

    private fun triggerAutosave() {
        val draft = DraftData(
            checker = checker.value,
            tm = tm.value,
            block = block.value,
            subBlock = subBlock.value,
            mrfNumber = mrfNumber.value,
            drrCode = drrCode.value,
            otherScheme = otherScheme.value,
            schemes = selectedSchemes.value.toList(),
            selectedMaterials = selectedMaterials.value.toList(),
            materialItems = selectedMaterials.value.map { name ->
                val form = materialFormState.value[name] ?: MaterialFields()
                MaterialItem(
                    name = name,
                    requested = form.requested,
                    received = form.received,
                    used = form.used,
                    remaining = form.remaining,
                    comment = form.comment
                )
            }
        )
        draftManager.saveDraft(draft)
    }

    fun saveEntry(onSuccess: (FieldEntry) -> Unit, onError: (String) -> Unit) {
        val chk = checker.value.trim()
        val tVal = tm.value
        val blk = block.value
        val sub = subBlock.value
        val mrf = mrfNumber.value.trim()
        val drr = drrCode.value.trim()
        val schemesList = selectedSchemes.value.toMutableList()
        if (otherScheme.value.trim().isNotEmpty()) {
            schemesList.add(otherScheme.value.trim())
        }

        if (chk.isEmpty()) {
            onError("Checker name is required.")
            return
        }
        if (tVal.isEmpty()) {
            onError("TM selection is required.")
            return
        }
        if (blk.isEmpty()) {
            onError("Block selection is required.")
            return
        }
        if (sub.isEmpty()) {
            onError("Sub-Block selection is required.")
            return
        }

        val list = mutableListOf<MaterialItem>()
        for (name in selectedMaterials.value) {
            val f = materialFormState.value[name] ?: continue
            if (f.requested.isEmpty() && f.received.isEmpty() && f.used.isEmpty()) continue
            
            // Validation
            val reqF = f.requested.toFloatOrNull()
            val recF = f.received.toFloatOrNull()
            val usedF = f.used.toFloatOrNull()

            if (f.requested.isNotEmpty() && reqF == null) {
                onError("Requested quantity must be a valid number for $name.")
                return
            }
            if (f.received.isNotEmpty() && recF == null) {
                onError("Received quantity must be a valid number for $name.")
                return
            }
            if (f.used.isNotEmpty() && usedF == null) {
                onError("Used quantity must be a valid number for $name.")
                return
            }

            list.add(
                MaterialItem(
                    name = name,
                    requested = f.requested,
                    received = f.received,
                    used = f.used,
                    remaining = f.remaining,
                    comment = f.comment
                )
            )
        }

        if (list.isEmpty()) {
            onError("Please enter quantities for at least one selected material.")
            return
        }

        val dateStr = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val entry = FieldEntry(
            id = UUID.randomUUID().toString(),
            checker = chk,
            tm = tVal,
            block = blk,
            subBlock = sub,
            mrfNumber = mrf,
            drrCode = drr,
            schemes = schemesList,
            materials = list,
            date = dateStr
        )

        viewModelScope.launch {
            try {
                dao.insertEntry(entry)
                clearForm()
                onSuccess(entry)
            } catch (e: Exception) {
                onError("Database error: ${e.message}")
            }
        }
    }

    fun deleteEntry(entry: FieldEntry) {
        viewModelScope.launch {
            dao.deleteEntryById(entry.id)
        }
    }

    fun deleteAllEntries() {
        viewModelScope.launch {
            dao.deleteAllEntries()
        }
    }
}
