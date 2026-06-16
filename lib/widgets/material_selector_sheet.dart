import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/app_state.dart';
import '../utils/constants.dart';

class MaterialSelectorSheet extends StatefulWidget {
  const MaterialSelectorSheet({super.key});

  @override
  State<MaterialSelectorSheet> createState() => _MaterialSelectorSheetState();
}

class _MaterialSelectorSheetState extends State<MaterialSelectorSheet> {
  String _searchQuery = '';
  final TextEditingController _customController = TextEditingController();

  @override
  void dispose() {
    _customController.dispose();
    super.override();
  }

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);
    
    final allList = [
      ...AppConstants.presetMaterials,
      ...appState.customMaterials
    ];

    final filteredList = allList.where((m) {
      return m.toLowerCase().contains(_searchQuery.toLowerCase());
    }).toList();

    return Container(
      padding: const EdgeInsets.all(16.0),
      height: MediaQuery.of(context).size.height * 0.82,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Select Materials',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              )
            ],
          ),
          const SizedBox(height: 8),

          // Search Field
          TextField(
            onChanged: (val) {
              setState(() {
                _searchQuery = val;
              });
            },
            decoration: const InputDecoration(
              labelText: 'Search materials...',
              prefixIcon: Icon(Icons.search),
            ),
          ),
          const SizedBox(height: 12),

          // Custom Material creation
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _customController,
                  decoration: const InputDecoration(
                    labelText: 'Add custom material name...',
                  ),
                ),
              ),
              const SizedBox(width: 8),
              ElevatedButton(
                onPressed: () {
                  final text = _customController.text.trim();
                  if (text.isNotEmpty) {
                    final success = appState.addCustomMaterial(text);
                    if (success) {
                      _customController.clear();
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Added custom material')),
                      );
                    } else {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Material already exists')),
                      );
                    }
                  }
                },
                child: const Text('➕ Add'),
              )
            ],
          ),
          const SizedBox(height: 10),

          Text(
            '${appState.selectedMaterials.length} Selected',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Theme.of(context).colorScheme.primary,
            ),
          ),
          const SizedBox(height: 10),

          // Scrollable list items
          Expanded(
            child: ListView.builder(
              itemCount: filteredList.length,
              itemBuilder: (context, index) {
                final material = filteredList[index];
                final isSelected = appState.selectedMaterials.contains(material);
                final isCustom = appState.customMaterials.contains(material);

                return CheckboxListTile(
                  value: isSelected,
                  title: Text(material),
                  secondary: isCustom
                      ? Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: Theme.of(context).colorScheme.surfaceVariant,
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text(
                            'Custom',
                            style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold),
                          ),
                        )
                      : null,
                  onChanged: (bool? value) {
                    if (value != null) {
                      appState.toggleMaterialSelected(material, value);
                    }
                  },
                );
              },
            ),
          ),
          const SizedBox(height: 12),

          SizedBox(
            width: double.infinity,
            height: 48,
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8.0),
                ),
              ),
              onPressed: () => Navigator.pop(context),
              child: const Text(
                'Done Selection',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
            ),
          )
        ],
      ),
    );
  }
}
