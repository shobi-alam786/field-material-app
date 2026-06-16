class AppConstants {
  static const List<String> tms = [
    'Ayaz',
    'Eliyes',
    'Sona Mia',
    'Arfat',
    'Ayub Khan',
    'Omer Farooq',
    'Nur Alom'
  ];

  static const Map<String, String> tmToBlock = {
    'Ayaz': 'A',
    'Eliyes': 'B',
    'Sona Mia': 'C',
    'Arfat': 'D',
    'Ayub Khan': 'E',
    'Omer Farooq': 'F',
    'Nur Alom': 'G',
  };

  static const Map<String, String> tmToDrrPrefix = {
    'Ayaz': 'C013-A-',
    'Eliyes': 'C013-B-',
    'Sona Mia': 'C013-C-',
    'Arfat': 'C013-D-',
    'Ayub Khan': 'C013-E-',
    'Omer Farooq': 'C013-F-',
    'Nur Alom': 'C013-G-',
  };

  static const Map<String, List<String>> blockToSubBlocks = {
    'A': ['A1', 'A2', 'A3', 'A4', 'A5', 'A6'],
    'B': ['B1', 'B2', 'B3', 'B4', 'B5', 'B6'],
    'C': ['C1', 'C2', 'C3', 'C4', 'C5'],
    'D': ['D1', 'D2', 'D3', 'D4', 'D5'],
    'E': ['E1', 'E2', 'E3', 'E4', 'E5'],
    'F': ['F1', 'F2', 'F3', 'F4', 'F5'],
    'G': ['G1', 'G2', 'G3', 'G4', 'G5'],
  };

  static const List<String> presetSchemes = [
    'Brick guide wall',
    'Bamboo crib wall',
    'Masonry Pathway',
    'Permeable block paving',
    'Bamboo fencing',
    'Drainage',
  ];

  static const List<String> presetMaterials = [
    'Cement',
    'Bricks',
    'Brick Chips',
    'Borak Bamboo',
    'Sand',
    'Muli Bamboo',
    '3mm Rope',
    '6mm Rope',
    'Gi wire',
    'Jute Bag',
    'Geo Roll',
    'Geo Bag',
    'Rebar 8mm',
    'Rebar 10mm',
    'Rebar 12mm',
    'Rebar 16mm',
    'Permeable block'
  ];
}
