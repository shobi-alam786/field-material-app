package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.FieldViewModel
import com.example.ui.MaterialFields
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.ExportHelper
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FieldViewModel = viewModel()
            val isDark by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FieldViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val savedEntries by viewModel.savedEntries.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Construction,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Field Material App",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.currentTab.value = 0 },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "New Entry") },
                    label = { Text("New Entry") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_new")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.currentTab.value = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (savedEntries.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = savedEntries.size.toString(),
                                            modifier = Modifier.testTag("badge_count")
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = "Saved Entries")
                        }
                    },
                    label = { Text("Saved Entries") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_saved")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> NewEntryScreen(viewModel)
                1 -> SavedEntriesScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(viewModel: FieldViewModel) {
    val context = LocalContext.current
    val checker by viewModel.checker.collectAsState()
    val tm by viewModel.tm.collectAsState()
    val block by viewModel.block.collectAsState()
    val subBlock by viewModel.subBlock.collectAsState()
    val mrfNumber by viewModel.mrfNumber.collectAsState()
    val drrCode by viewModel.drrCode.collectAsState()
    val otherScheme by viewModel.otherScheme.collectAsState()
    val selectedSchemes by viewModel.selectedSchemes.collectAsState()
    val selectedMaterials by viewModel.selectedMaterials.collectAsState()
    val customMaterials by viewModel.customMaterials.collectAsState()
    val materialFormState by viewModel.materialFormState.collectAsState()

    var showMaterialSelector by remember { mutableStateOf(false) }
    var tmExpanded by remember { mutableStateOf(false) }
    var blockExpanded by remember { mutableStateOf(false) }
    var subExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Project Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Project Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Checker Name Input
                    OutlinedTextField(
                        value = checker,
                        onValueChange = { viewModel.setChecker(it) },
                        label = { Text("Checker Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("checker_input"),
                        singleLine = true
                    )

                    // TM Dropdown Matcher
                    ExposedDropdownMenuBox(
                        expanded = tmExpanded,
                        onExpandedChange = { tmExpanded = !tmExpanded },
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = tm,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select TM") },
                            leadingIcon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tmExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("tm_dropdown"),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(
                            expanded = tmExpanded,
                            onDismissRequest = { tmExpanded = false }
                        ) {
                            AppConstants.TMs.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        viewModel.setTm(name)
                                        tmExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Block Selection dropdown (Auto-selected based on TM)
                    ExposedDropdownMenuBox(
                        expanded = blockExpanded,
                        onExpandedChange = { blockExpanded = !blockExpanded },
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = if (block.isEmpty()) "Select TM First" else block,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Block") },
                            leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = blockExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("block_dropdown"),
                            enabled = tm.isNotEmpty()
                        )
                        ExposedDropdownMenu(
                            expanded = blockExpanded && tm.isNotEmpty(),
                            onDismissRequest = { blockExpanded = false }
                        ) {
                            AppConstants.blockToSubBlocks.keys.forEach { blockVal ->
                                DropdownMenuItem(
                                    text = { Text(blockVal) },
                                    onClick = {
                                        viewModel.setBlock(blockVal)
                                        blockExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Sub-Block Selection
                    ExposedDropdownMenuBox(
                        expanded = subExpanded,
                        onExpandedChange = { subExpanded = !subExpanded },
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = if (subBlock.isEmpty()) "Select Sub-Block" else subBlock,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sub-Block") },
                            leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("subblock_dropdown"),
                            enabled = block.isNotEmpty()
                        )
                        ExposedDropdownMenu(
                            expanded = subExpanded && block.isNotEmpty(),
                            onDismissRequest = { subExpanded = false }
                        ) {
                            val list = AppConstants.blockToSubBlocks[block] ?: emptyList()
                            list.forEach { subVal ->
                                DropdownMenuItem(
                                    text = { Text(subVal) },
                                    onClick = {
                                        viewModel.setSubBlock(subVal)
                                        subExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // MRF number
                    OutlinedTextField(
                        value = mrfNumber,
                        onValueChange = { viewModel.setMrfNumber(it) },
                        label = { Text("MRF Number (Optional)") },
                        leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("mrf_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // DRR Code Prefix code
                    OutlinedTextField(
                        value = drrCode,
                        onValueChange = { viewModel.setDrrCode(it) },
                        label = { Text("DRR CODE (Auto-formatted)") },
                        leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drr_input"),
                        singleLine = true,
                        placeholder = { Text("e.g. C013-X-") }
                    )
                }
            }
        }

        // Section 2: Schemes chooser
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Schemes (Select Multiples)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppConstants.schemes.forEach { scheme ->
                            FilterChip(
                                selected = selectedSchemes.contains(scheme),
                                onClick = { viewModel.toggleScheme(scheme) },
                                label = { Text(scheme) },
                                leadingIcon = {
                                    if (selectedSchemes.contains(scheme)) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("scheme_chip_${scheme.replace(" ", "_")}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = otherScheme,
                        onValueChange = { viewModel.setOtherScheme(it) },
                        label = { Text("Other Scheme Name (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Extension, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("other_scheme_input"),
                        singleLine = true
                    )
                }
            }
        }

        // Section 3: Material Tracking & Selector Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Materials Tracking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showMaterialSelector = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("material_select_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Select", fontSize = 12.sp)
                        }
                    }

                    if (selectedMaterials.isEmpty()) {
                        Text(
                            text = "No materials selected yet. Tap 'Select' to add tracking forms.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${selectedMaterials.size} tracking card(s) active:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Material Form Sheets list dynamically
        items(selectedMaterials.toList()) { materialName ->
            val fields = materialFormState[materialName] ?: MaterialFields()
            val usedExceedsReceived = try {
                val rec = fields.received.toFloatOrNull() ?: 0f
                val usd = fields.used.toFloatOrNull() ?: 0f
                usd > rec
            } catch (e: Exception) {
                false
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(
                    1.dp,
                    if (usedExceedsReceived) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = materialName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (usedExceedsReceived) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { viewModel.toggleMaterialSelected(materialName, false) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Material",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid Columns for inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fields.requested,
                            onValueChange = { viewModel.updateMaterialField(materialName, requested = it) },
                            label = { Text("Req", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("req_${materialName.replace(" ", "_")}"),
                            maxLines = 1,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                        )
                        OutlinedTextField(
                            value = fields.received,
                            onValueChange = { viewModel.updateMaterialField(materialName, received = it) },
                            label = { Text("Rec", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("rec_${materialName.replace(" ", "_")}"),
                            maxLines = 1,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                        )
                        OutlinedTextField(
                            value = fields.used,
                            onValueChange = { viewModel.updateMaterialField(materialName, used = it) },
                            label = { Text("Used", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("used_${materialName.replace(" ", "_")}"),
                            maxLines = 1,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                        )
                        // Remaining box with dynamic background highlight color
                        val highlightColor = if (usedExceedsReceived) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textHighlight = if (usedExceedsReceived) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        OutlinedTextField(
                            value = fields.remaining,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rem", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("rem_${materialName.replace(" ", "_")}"),
                            maxLines = 1,
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 13.sp,
                                color = textHighlight,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = highlightColor,
                                unfocusedContainerColor = highlightColor,
                                disabledContainerColor = highlightColor,
                                focusedBorderColor = if (usedExceedsReceived) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                unfocusedBorderColor = if (usedExceedsReceived) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Comment field
                    OutlinedTextField(
                        value = fields.comment,
                        onValueChange = { viewModel.updateMaterialField(materialName, comment = it) },
                        label = { Text("Comment / Location Notes", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comment_${materialName.replace(" ", "_")}"),
                        maxLines = 1,
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                }
            }
        }

        // Action triggers
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveEntry(
                                onSuccess = {
                                    Toast.makeText(context, "Entry saved successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Entry", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.saveEntry(
                                onSuccess = { entry ->
                                    val text = ExportHelper.generateWhatsAppText(entry)
                                    ExportHelper.shareViaWhatsApp(context, text)
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("whatsapp_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearForm() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("clear_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Form", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Modal Searchable selection dialog for Materials
    if (showMaterialSelector) {
        MaterialSelectorSheet(
            selectedMaterials = selectedMaterials,
            customMaterials = customMaterials,
            onDismiss = { showMaterialSelector = false },
            onToggleMaterial = { name, selected -> viewModel.toggleMaterialSelected(name, selected) },
            onAddCustom = { viewModel.addCustomMaterial(it) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MaterialSelectorSheet(
    selectedMaterials: Set<String>,
    customMaterials: List<String>,
    onDismiss: () -> Unit,
    onToggleMaterial: (String, Boolean) -> Unit,
    onAddCustom: (String) -> Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var customMaterialInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val allPresetAndCustom = remember(customMaterials) {
        AppConstants.presetMaterials + customMaterials
    }

    val filteredList = allPresetAndCustom.filter {
        it.lowercase(Locale.getDefault()).contains(searchQuery.lowercase(Locale.getDefault()))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Materials",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search materials...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("material_search_input"),
                    singleLine = true
                )

                // Add Custom material section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customMaterialInput,
                        onValueChange = { customMaterialInput = it },
                        placeholder = { Text("Add custom material name...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_material_input"),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customMaterialInput.trim().isNotEmpty()) {
                                val success = onAddCustom(customMaterialInput)
                                if (success) {
                                    Toast.makeText(context, "Added custom material!", Toast.LENGTH_SHORT).show()
                                    customMaterialInput = ""
                                } else {
                                    Toast.makeText(context, "Material already exists or has invalid name.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_custom_material_button")
                    ) {
                        Text("➕ Add", fontSize = 12.sp)
                    }
                }

                // Selected count
                Text(
                    text = "${selectedMaterials.size} Selected",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Scrollable Checklist
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredList) { mat ->
                        val isChecked = selectedMaterials.contains(mat)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleMaterial(mat, !isChecked) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { onToggleMaterial(mat, it) },
                                modifier = Modifier.testTag("checkbox_${mat.replace(" ", "_")}")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mat,
                                modifier = Modifier.weight(1f),
                                fontSize = 15.sp,
                                color = if (customMaterials.contains(mat)) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                            )
                            if (customMaterials.contains(mat)) {
                                Text(
                                    text = "Custom",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Done trigger button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("material_selector_done"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Done Selection", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SavedEntriesScreen(viewModel: FieldViewModel) {
    val context = LocalContext.current
    val savedEntries by viewModel.savedEntries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var entryToDelete by remember { mutableStateOf<FieldEntry?>(null) }
    var showDeleteAllDraftConfirm by remember { mutableStateOf(false) }

    val filteredEntries = remember(savedEntries, searchQuery) {
        savedEntries.filter { ent ->
            ent.checker.lowercase().contains(searchQuery.lowercase()) ||
            ent.tm.lowercase().contains(searchQuery.lowercase()) ||
            ent.block.lowercase().contains(searchQuery.lowercase()) ||
            ent.subBlock.lowercase().contains(searchQuery.lowercase()) ||
            ent.drrCode.lowercase().contains(searchQuery.lowercase()) ||
            ent.materials.any { it.name.lowercase().contains(searchQuery.lowercase()) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search and Filter box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            label = { Text("Search filter historic records...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("saved_search_input"),
            singleLine = true
        )

        // Bulk Actions Row for Exporting
        if (filteredEntries.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val csv = ExportHelper.entriesToCsv(savedEntries)
                        ExportHelper.saveFileToDownloads(context, "Field_Material_Entries_Excel.csv", "text/csv", csv)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_excel"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Excel CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val csv = ExportHelper.entriesToCsv(savedEntries)
                        ExportHelper.saveFileToDownloads(context, "Field_Material_Entries.csv", "text/csv", csv)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_csv"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Export CSV", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        val json = ExportHelper.entriesToJson(savedEntries)
                        ExportHelper.saveFileToDownloads(context, "Field_Material_Entries.json", "application/json", json)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_json"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Export JSON", fontSize = 11.sp)
                }
            }

            OutlinedButton(
                onClick = {
                    showDeleteAllDraftConfirm = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("delete_all_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete All Entries", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Checklist of historical records
        if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No records match search filter." else "No records tracked yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header Summaries
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${entry.block} - ${entry.subBlock} | TM: ${entry.tm}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Checker: ${entry.checker}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = entry.date,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val text = ExportHelper.generateWhatsAppText(entry)
                                            ExportHelper.shareViaWhatsApp(context, text)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.SendToMobile,
                                            contentDescription = "Share WhatsApp",
                                            tint = Color(0xFF25D366),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            entryToDelete = entry
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Expandable materials tracking list details
                            if (isExpanded) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                if (entry.mrfNumber.isNotEmpty() || entry.drrCode.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        if (entry.mrfNumber.isNotEmpty()) {
                                            Text(
                                                text = "MRF: ${entry.mrfNumber}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (entry.drrCode.isNotEmpty()) {
                                            Text(
                                                text = "DRR: ${entry.drrCode}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }

                                if (entry.schemes.isNotEmpty()) {
                                    Text(
                                        text = "Schemes: ${entry.schemes.joinToString(", ")}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                Text(
                                    text = "Tracked Materials:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                entry.materials.forEach { mat ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = mat.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                if (mat.requested.isNotEmpty()) {
                                                    Text("Req: ${mat.requested}", fontSize = 11.sp)
                                                }
                                                if (mat.received.isNotEmpty()) {
                                                    Text("Rec: ${mat.received}", fontSize = 11.sp)
                                                }
                                                if (mat.used.isNotEmpty()) {
                                                    Text("Used: ${mat.used}", fontSize = 11.sp)
                                                }
                                                if (mat.remaining.isNotEmpty()) {
                                                    val isNegative = (mat.remaining.toFloatOrNull() ?: 0f) < 0f
                                                    Text(
                                                        text = "Rem: ${mat.remaining}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            if (mat.comment.isNotEmpty()) {
                                                Text(
                                                    text = "Note: ${mat.comment}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteAllDraftConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDraftConfirm = false },
            title = { Text("Delete All Records") },
            text = { Text("Are you absolutely sure you want to delete all entries? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllEntries()
                        showDeleteAllDraftConfirm = false
                        Toast.makeText(context, "All historical records deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDraftConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Entry") },
            text = { Text("Delete this tracked record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entry)
                        entryToDelete = null
                        Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
