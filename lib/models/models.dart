class MaterialItem {
  final String name;
  final String requested;
  final String received;
  final String used;
  final String remaining;
  final String comment;

  MaterialItem({
    required this.name,
    this.requested = '',
    this.received = '',
    this.used = '',
    this.remaining = '',
    this.comment = '',
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'requested': requested,
      'received': received,
      'used': used,
      'remaining': remaining,
      'comment': comment,
    };
  }

  factory MaterialItem.fromJson(Map<String, dynamic> json) {
    return MaterialItem(
      name: json['name'] ?? '',
      requested: json['requested'] ?? '',
      received: json['received'] ?? '',
      used: json['used'] ?? '',
      remaining: json['remaining'] ?? '',
      comment: json['comment'] ?? '',
    );
  }
}

class FieldEntry {
  final String id;
  final String checker;
  final String tm;
  final String block;
  final String subBlock;
  final String mrfNumber;
  final String drrCode;
  final List<String> schemes;
  final List<MaterialItem> materials;
  final String date;

  FieldEntry({
    required this.id,
    required this.checker,
    required this.tm,
    required this.block,
    required this.subBlock,
    required this.mrfNumber,
    required this.drrCode,
    required this.schemes,
    required this.materials,
    required this.date,
  });

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'checker': checker,
      'tm': tm,
      'block': block,
      'subBlock': subBlock,
      'mrfNumber': mrfNumber,
      'drrCode': drrCode,
      'schemes': schemes,
      'materials': materials.map((m) => m.toJson()).toList(),
      'date': date,
    };
  }

  factory FieldEntry.fromJson(Map<String, dynamic> json) {
    var rawMaterials = json['materials'] as List? ?? [];
    List<MaterialItem> parsedMaterials = rawMaterials
        .map((m) => MaterialItem.fromJson(Map<String, dynamic>.from(m)))
        .toList();

    return FieldEntry(
      id: json['id'] ?? '',
      checker: json['checker'] ?? '',
      tm: json['tm'] ?? '',
      block: json['block'] ?? '',
      subBlock: json['subBlock'] ?? '',
      mrfNumber: json['mrfNumber'] ?? '',
      drrCode: json['drrCode'] ?? '',
      schemes: List<String>.from(json['schemes'] ?? []),
      materials: parsedMaterials,
      date: json['date'] ?? '',
    );
  }
}
