import 'package:flutter/material.dart';
import '../models/models.dart';
import 'storage_service.dart';

class FormFieldsState {
  String requested;
  String received;
  String used;
  String remaining;
  String comment;

  FormFieldsState({
    this.requested = '',
    this.received = '',
    this.used = '',
    this.remaining = '',
    this.comment = '',
  });

  Map<String, String> toMap() {
    return {
      'requested': requested,
      'received': received,
      'used': used,
      'remaining': remaining,
      'comment': comment,
    };
  }
}

class AppState extends ChangeNotifier {
  List<FieldEntry> _entries = [];
  List<FieldEntry> get entries => _entries;

  // Form selections and texts
  String _checker = '';
  String get checker => _checker;

  String _tm = '';
  String get tm => _tm;

  String _block = '';
  String get block => _block;

  String _subBlock = '';
  String get subBlock => _subBlock;

  String _mrfNumber = '';
  String get mrfNumber => _mrfNumber;

  String _drrCode = '';
  String get drrCode => _drrCode;

  String _otherScheme = '';
  String get otherScheme => _otherScheme;

  Set<String> _selectedSchemes = {};
  Set<String> get selectedSchemes => _selectedSchemes;

  Set<String> _selectedMaterials = {};
  Set<String> get selectedMaterials => _selectedMaterials;

  List<String> _customMaterials = [];
  List<String> get customMaterials => _customMaterials;

  // Material Form state mapping
  Map<String, FormFieldsState> _materialFormState = {};
  Map<String, FormFieldsState> get materialFormState => _materialFormState;

  // UI preferences
  int _currentTab = 0;
  int get currentTab => _currentTab;

  bool _isDarkMode = false;
  bool get isDarkMode => _isDarkMode;

  String _searchQuery = '';
  String get searchQuery => _searchQuery;

  AppState() {
    _initialize();
  }

  Future<void> _initialize() async {
    _isDarkMode = await StorageService.loadThemeMode();
    _checker = await StorageService.loadLastChecker();
    _entries = await StorageService.loadEntries();
    
    // Restore Draft
    final draft = await StorageService.loadDraft();
    if (draft != null) {
      _checker = draft['checker'] ?? _checker;
      _tm = draft['tm'] ?? '';
      _block = draft['block'] ?? '';
      _subBlock = draft['subBlock'] ?? '';
      _mrfNumber = draft['mrfNumber'] ?? '';
      _drrCode = draft['drrCode'] ?? '';
      _otherScheme = draft['otherScheme'] ?? '';
      _selectedSchemes = Set<String>.from(draft['schemes'] ?? []);
      _selectedMaterials = Set<String>.from(draft['selectedMaterials'] ?? []);
      
      final rawMaterialsList = draft['materials'] as List? ?? [];
      for (var mat in rawMaterialsList) {
        final name = mat['name'];
        if (name != null) {
          _materialFormState[name] = FormFieldsState(
            requested: mat['requested'] ?? '',
            received: mat['received'] ?? '',
            used: mat['used'] ?? '',
            remaining: mat['remaining'] ?? '',
            comment: mat['comment'] ?? '',
          );
        }
      }
      
      // Determine custom materials from selected list
      _customMaterials = _selectedMaterials
          .where((m) => !_isPresetMaterial(m))
          .toList();
    }
    notifyListeners();
  }

  bool _isPresetMaterial(String m) {
    const presets = [
      'Cement', 'Bricks', 'Brick Chips', 'Borak Bamboo', 'Sand', 'Muli Bamboo',
      '3mm Rope', '6mm Rope', 'Gi wire', 'Jute Bag', 'Geo Roll', 'Geo Bag',
      'Rebar 8mm', 'Rebar 10mm', 'Rebar 12mm', 'Rebar 16mm', 'Permeable block'
    ];
    return presets.contains(m);
  }

  // Setters
  void toggleDarkMode() {
    _isDarkMode = !_isDarkMode;
    StorageService.saveThemeMode(_isDarkMode);
    notifyListeners();
  }

  void setTab(int index) {
    _currentTab = index;
    notifyListeners();
  }

  void setSearchQuery(String q) {
    _searchQuery = q;
    notifyListeners();
  }

  void setChecker(String value) {
    _checker = value;
    StorageService.saveLastChecker(value);
    _triggerAutosave();
    notifyListeners();
  }

  void setTm(String value, String calculatedBlock, String defaultDrrPrefix) {
    _tm = value;
    _block = calculatedBlock;
    _subBlock = '';
    _drrCode = defaultDrrPrefix;
    _triggerAutosave();
    notifyListeners();
  }

  void setBlock(String value) {
    _block = value;
    _subBlock = '';
    _drrCode = '';
    _triggerAutosave();
    notifyListeners();
  }

  void setSubBlock(String value, String defaultPrefix) {
    _subBlock = value;
    if (!_drrCode.startsWith(defaultPrefix)) {
      _drrCode = defaultPrefix;
    }
    _triggerAutosave();
    notifyListeners();
  }

  void setMrfNumber(String value) {
    _mrfNumber = value;
    _triggerAutosave();
    notifyListeners();
  }

  void setDrrCode(String value, String prefix) {
    String formatted = value.toUpperCase();
    if (prefix.isNotEmpty && formatted.startsWith(prefix)) {
      final suffix = formatted.substring(prefix.length).replaceAll(RegExp(r'[^0-9]'), '');
      if (suffix.length > 6) {
        formatted = '$prefix${suffix.substring(0, 6)}-${suffix.substring(6)}';
      } else {
        formatted = '$prefix$suffix';
      }
    }
    _drrCode = formatted;
    _triggerAutosave();
    notifyListeners();
  }

  void setOtherScheme(String value) {
    _otherScheme = value;
    _triggerAutosave();
    notifyListeners();
  }

  void toggleScheme(String scheme) {
    if (_selectedSchemes.contains(scheme)) {
      _selectedSchemes.remove(scheme);
    } else {
      _selectedSchemes.add(scheme);
    }
    _triggerAutosave();
    notifyListeners();
  }

  bool addCustomMaterial(String name) {
    final trimmed = name.trim();
    if (trimmed.isEmpty) return false;
    if (_customMaterials.contains(trimmed)) return false;
    
    _customMaterials.add(trimmed);
    toggleMaterialSelected(trimmed, true);
    return true;
  }

  void toggleMaterialSelected(String name, bool isSelected) {
    if (isSelected) {
      _selectedMaterials.add(name);
      if (!_materialFormState.containsKey(name)) {
        _materialFormState[name] = FormFieldsState();
      }
    } else {
      _selectedMaterials.remove(name);
      _materialFormState.remove(name);
    }
    _triggerAutosave();
    notifyListeners();
  }

  void updateMaterialField(
    String name, {
    String? requested,
    String? received,
    String? used,
    String? comment,
  }) {
    final fields = _materialFormState[name] ?? FormFieldsState();
    if (requested != null) fields.requested = requested;
    if (received != null) fields.received = received;
    if (used != null) fields.used = used;
    if (comment != null) fields.comment = comment;

    // Remaining logic
    final recVal = double.tryParse(fields.received) ?? 0.0;
    final usedVal = double.tryParse(fields.used) ?? 0.0;

    if (fields.received.isEmpty && fields.used.isEmpty) {
      fields.remaining = '';
    } else if (usedVal > recVal) {
      final diff = recVal - usedVal;
      fields.remaining = diff % 1.0 == 0.0 ? diff.toInt().toString() : diff.toStringAsFixed(2);
    } else {
      final rem = recVal - usedVal;
      fields.remaining = rem % 1.0 == 0.0 ? rem.toInt().toString() : rem.toStringAsFixed(2);
    }

    _materialFormState[name] = fields;
    _triggerAutosave();
    notifyListeners();
  }

  void clearForm() {
    _tm = '';
    _block = '';
    _subBlock = '';
    _mrfNumber = '';
    _drrCode = '';
    _otherScheme = '';
    _selectedSchemes.clear();
    _selectedMaterials.clear();
    _materialFormState.clear();
    _customMaterials.clear();
    StorageService.clearDraft();
    notifyListeners();
  }

  void _triggerAutosave() {
    final list = _selectedMaterials.map((name) {
      final fields = _materialFormState[name] ?? FormFieldsState();
      return {
        'name': name,
        'requested': fields.requested,
        'received': fields.received,
        'used': fields.used,
        'remaining': fields.remaining,
        'comment': fields.comment,
      };
    }).toList();

    StorageService.saveDraft({
      'checker': _checker,
      'tm': _tm,
      'block': _block,
      'subBlock': _subBlock,
      'mrfNumber': _mrfNumber,
      'drrCode': _drrCode,
      'otherScheme': _otherScheme,
      'schemes': _selectedSchemes.toList(),
      'selectedMaterials': _selectedMaterials.toList(),
      'materials': list,
    });
  }

  Future<bool> saveEntry() async {
    if (_checker.trim().isEmpty || _tm.isEmpty || _block.isEmpty || _subBlock.isEmpty) {
      return false;
    }

    final list = <MaterialItem>[];
    for (var name in _selectedMaterials) {
      final fields = _materialFormState[name];
      if (fields == null) continue;
      if (fields.requested.isEmpty && fields.received.isEmpty && fields.used.isEmpty) continue;

      list.add(MaterialItem(
        name: name,
        requested: fields.requested,
        received: fields.received,
        used: fields.used,
        remaining: fields.remaining,
        comment: fields.comment,
      ));
    }

    if (list.isEmpty) return false;

    final dateStr = '${DateTime.now().day.toString().padLeft(2, '0')}/${DateTime.now().month.toString().padLeft(2, '0')}/${DateTime.now().year}, ${DateTime.now().hour % 12 == 0 ? 12 : DateTime.now().hour % 12}:${DateTime.now().minute.toString().padLeft(2, '0')} ${DateTime.now().hour >= 12 ? 'PM' : 'AM'}';

    final finalSchemes = _selectedSchemes.toList();
    if (_otherScheme.trim().isNotEmpty) {
      finalSchemes.add(_otherScheme.trim());
    }

    final entry = FieldEntry(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      checker: _checker.trim(),
      tm: _tm,
      block: _block,
      subBlock: _subBlock,
      mrfNumber: _mrfNumber.trim(),
      drrCode: _drrCode.trim(),
      schemes: finalSchemes,
      materials: list,
      date: dateStr,
    );

    _entries.insert(0, entry);
    await StorageService.saveEntries(_entries);
    clearForm();
    return true;
  }

  Future<void> deleteEntry(String id) async {
    _entries.removeWhere((e) => e.id == id);
    await StorageService.saveEntries(_entries);
    notifyListeners();
  }

  Future<void> deleteAllEntries() async {
    _entries.clear();
    await StorageService.saveEntries(_entries);
    notifyListeners();
  }
}
