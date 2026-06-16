import 'package:flutter/material.dart';

class AppTheme {
  // Light Theme Configuration
  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,
      colorScheme: const ColorScheme.light(
        primary: Color(0xFF1976D2),
        secondary: Color(0xFF4CAF50),
        tertiary: Color(0xFF00796B),
        background: Color(0xFFF5F7FA),
        surface: Color(0xFFFFFFFF),
        error: Color(0xFFD32F2F),
      ),
      cardTheme: CardTheme(
        elevation: 2,
        shape: RoundedCornerShape(),
      ),
      inputDecorationTheme: InputDecorationTheme(
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8.0),
        ),
        filled: true,
        fillColor: const Color(0xFFF9F9F9),
      ),
    );
  }

  // Dark Theme Configuration
  static ThemeData get darkTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: const ColorScheme.dark(
        primary: Color(0xFF90CAF9),
        secondary: Color(0xFF81C784),
        tertiary: Color(0xFF4DB6AC),
        background: Color(0xFF121212),
        surface: Color(0xFF1E1E1E),
        error: Color(0xFFEF5350),
      ),
      cardTheme: CardTheme(
        elevation: 4,
        shape: RoundedCornerShape(),
      ),
      inputDecorationTheme: InputDecorationTheme(
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8.0),
        ),
        filled: true,
        fillColor: const Color(0xFF2C2C2C),
      ),
    );
  }

  static RoundedRectangleBorder RoundedCornerShape() {
    return RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(12.0),
    );
  }
}
