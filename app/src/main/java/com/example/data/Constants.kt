package com.example.data

object AppConstants {
    val TMs = listOf("Ayaz", "Eliyes", "Sona Mia", "Arfat", "Ayub Khan", "Omer Farooq", "Nur Alom")
    
    val tmToBlock = mapOf(
        "Ayaz" to "A",
        "Eliyes" to "B",
        "Sona Mia" to "C",
        "Arfat" to "D",
        "Ayub Khan" to "E",
        "Omer Farooq" to "F",
        "Nur Alom" to "G"
    )

    val tmToDrrPrefix = mapOf(
        "Ayaz" to "C013-A-",
        "Eliyes" to "C013-B-",
        "Sona Mia" to "C013-C-",
        "Arfat" to "C013-D-",
        "Ayub Khan" to "C013-E-",
        "Omer Farooq" to "C013-F-",
        "Nur Alom" to "C013-G-"
    )

    val blockToSubBlocks = mapOf(
        "A" to listOf("A1", "A2", "A3", "A4", "A5", "A6"),
        "B" to listOf("B1", "B2", "B3", "B4", "B5", "B6"),
        "C" to listOf("C1", "C2", "C3", "C4", "C5"),
        "D" to listOf("D1", "D2", "D3", "D4", "D5"),
        "E" to listOf("E1", "E2", "E3", "E4", "E5"),
        "F" to listOf("F1", "F2", "F3", "F4", "F5"),
        "G" to listOf("G1", "G2", "G3", "G4", "G5")
    )

    val schemes = listOf(
        "Brick guide wall",
        "Bamboo crib wall",
        "Masonry Pathway",
        "Permeable block paving",
        "Bamboo fencing",
        "Drainage"
    )

    val presetMaterials = listOf(
        "Cement",
        "Bricks",
        "Brick Chips",
        "Borak Bamboo",
        "Sand",
        "Muli Bamboo",
        "3mm Rope",
        "6mm Rope",
        "Gi wire",
        "Jute Bag",
        "Geo Roll",
        "Geo Bag",
        "Rebar 8mm",
        "Rebar 10mm",
        "Rebar 12mm",
        "Rebar 16mm",
        "Permeable block"
    )
}
