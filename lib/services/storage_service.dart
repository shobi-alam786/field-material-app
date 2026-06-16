import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/models.dart';

class StorageService {
  static const String _keyDraft = 'material_draft';
  static const String _keyEntries = 'material_entries';
  static const String _keyDarkMode = 'dark_mode';
  static const String _keyLastChecker = 'last_checker';

  // --- Theme Mode ---
  static Future<bool> loadThemeMode() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyDarkMode) ?? false;
  }

  static Future<void> saveThemeMode(bool isDark) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyDarkMode, isDark);
  }

  // --- Last Checker ---
  static Future<String> loadLastChecker() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyLastChecker) ?? '';
  }

  static Future<void> saveLastChecker(String checkerName) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyLastChecker, checkerName);
  }

  // --- Draft Management ---
  static Future<Map<String, dynamic>?> loadDraft() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_keyDraft);
    if (raw == null) return null;
    try {
      return jsonDecode(raw) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }

  static Future<void> saveDraft(Map<String, dynamic> draftData) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyDraft, jsonEncode(draftData));
  }

  static Future<void> clearDraft() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_keyDraft);
  }

  // --- Saved Entries Management ---
  static Future<List<FieldEntry>> loadEntries() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_keyEntries);
    if (raw == null) return [];
    try {
      final list = jsonDecode(raw) as List;
      return list.map((item) => FieldEntry.fromJson(Map<String, dynamic>.from(item))).toList();
    } catch (_) {
      return [];
    }
  }

  static Future<void> saveEntries(List<FieldEntry> entries) async {
    final prefs = await SharedPreferences.getInstance();
    final raw = jsonEncode(entries.map((e) => e.toJson()).toList());
    await prefs.setString(_keyEntries, raw);
  }
}
