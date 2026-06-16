import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/app_state.dart';
import '../services/export_service.dart';
import '../utils/constants.dart';
import '../widgets/section_card.dart';
import '../widgets/material_form_widget.dart';
import '../widgets/material_selector_sheet.dart';

class EntryFormScreen extends StatefulWidget {
  const EntryFormScreen({super.key});

  @override
  State<EntryFormScreen> createState() => _EntryFormScreenState();
}

class _EntryFormScreenState extends State<EntryFormScreen> {
  final _checkerController = TextEditingController();
  final _mrfController = TextEditingController();
  final _drrController = TextEditingController();
  final _otherSchemeController = TextEditingController();

  @override
  void initState() {
    super.initState();
    final appState = Provider.of<AppState>(context, listen: false);
    _checkerController.text = appState.checker;
    _mrfController.text = appState.mrfNumber;
    _drrController.text = appState.drrCode;
    _otherSchemeController.text = appState.otherScheme;
  }

  @override
  void dispose() {
    _checkerController.dispose();
    _mrfController.dispose();
    _drrController.dispose();
    _otherSchemeController.dispose();
    super.dispose();
  }

  void _showMaterialsSelector(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        return const MaterialSelectorSheet();
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);

    // Watchers to update local text controllers on draft restore or reset
    if (_checkerController.text != appState.checker) {
      _checkerController.text = appState.checker;
    }
    if (_mrfController.text != appState.mrfNumber) {
      _mrfController.text = appState.mrfNumber;
    }
    if (_drrController.text != appState.drrCode) {
      _drrController.text = appState.drrCode;
    }
    if (_otherSchemeController.text != appState.otherScheme) {
      _otherSchemeController.text = appState.otherScheme;
    }

    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
      children: [
        // Section 1: Project Details Form
        SectionCard(
          title: 'Project Details',
          icon: Icons.folder,
          iconColor: Theme.of(context).colorScheme.primary,
          child: Column(
            children: [
              // Checker
              TextFormField(
                controller: _checkerController,
                decoration: const InputDecoration(
                  labelText: 'Checker (Name)',
                  prefixIcon: Icon(Icons.person),
                ),
                onChanged: (val) {
                  appState.setChecker(val);
                },
              ),
              const SizedBox(height: 12),

              // TM dropdown
              DropdownButtonFormField<String>(
                value: appState.tm.isEmpty ? null : appState.tm,
                decoration: const InputDecoration(
                  labelText: 'TM (Select)',
                  prefixIcon: Icon(Icons.supervisor_account),
                ),
                items: AppConstants.tms.map((name) {
                  return DropdownMenuItem<String>(
                    value: name,
                    child: Text(name),
                  );
                }).toList(),
                onChanged: (val) {
                  if (val != null) {
                    final block = AppConstants.tmToBlock[val] ?? '';
                    final prefix = AppConstants.tmToDrrPrefix[val] ?? '';
                    appState.setTm(val, block, prefix);
                  }
                },
              ),
              const SizedBox(height: 12),

              // Block Selector
              DropdownButtonFormField<String>(
                value: appState.block.isEmpty ? null : appState.block,
                decoration: const InputDecoration(
                  labelText: 'Block',
                  prefixIcon: Icon(Icons.grid_on),
                ),
                items: AppConstants.blockToSubBlocks.keys.map((name) {
                  return DropdownMenuItem<String>(
                    value: name,
                    child: Text(name),
                  );
                }).toList(),
                onChanged: appState.tm.isNotEmpty
                    ? (val) {
                        if (val != null) {
                          appState.setBlock(val);
                        }
                      }
                    : null,
              ),
              const SizedBox(height: 12),

              // Sub Block Dropdown Selector
              DropdownButtonFormField<String>(
                value: appState.subBlock.isEmpty ? null : appState.subBlock,
                decoration: const InputDecoration(
                  labelText: 'Sub-Block',
                  prefixIcon: Icon(Icons.layers),
                ),
                items: (AppConstants.blockToSubBlocks[appState.block] ?? [])
                    .map((sub) {
                  return DropdownMenuItem<String>(
                    value: sub,
                    child: Text(sub),
                  );
                }).toList(),
                onChanged: appState.block.isNotEmpty
                    ? (val) {
                        if (val != null) {
                          final prefix = AppConstants.tmToDrrPrefix[appState.tm] ?? '';
                          appState.setSubBlock(val, prefix);
                        }
                      }
                    : null,
              ),
              const SizedBox(height: 12),

              // MRF Number
              TextFormField(
                controller: _mrfController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'MRF Number (Optional)',
                  prefixIcon: Icon(Icons.format_list_numbered),
                ),
                onChanged: (val) {
                  appState.setMrfNumber(val);
                },
              ),
              const SizedBox(height: 12),

              // DRR Code
              TextFormField(
                controller: _drrController,
                decoration: const InputDecoration(
                  labelText: 'DRR-CODE',
                  prefixIcon: Icon(Icons.confirmation_number),
                ),
                onChanged: (val) {
                  final prefix = AppConstants.tmToDrrPrefix[appState.tm] ?? '';
                  appState.setDrrCode(val, prefix);
                },
              ),
            ],
          ),
        ),

        // Section 2: Schemes Chooser Checklist
        SectionCard(
          title: 'Type of Scheme (Choose Multiple)',
          icon: Icons.category,
          iconColor: Theme.of(context).colorScheme.secondary,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Wrap(
                spacing: 8.0,
                runSpacing: 4.0,
                children: AppConstants.presetSchemes.map((scheme) {
                  final isSelected = appState.selectedSchemes.contains(scheme);
                  return FilterChip(
                    label: Text(scheme),
                    selected: isSelected,
                    onSelected: (selected) {
                      appState.toggleScheme(scheme);
                    },
                  );
                }).toList(),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _otherSchemeController,
                decoration: const InputDecoration(
                  labelText: 'Other scheme (if not listed)',
                  prefixIcon: Icon(Icons.extension),
                ),
                onChanged: (val) {
                  appState.setOtherScheme(val);
                },
              ),
            ],
          ),
        ),

        // Section 3: Materials Form generator Title and Select Trigger
        SectionCard(
          title: 'Material Tracking',
          icon: Icons.inventory,
          iconColor: Theme.of(context).colorScheme.tertiary,
          child: Column(
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Select Tracked Materials:',
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                  ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8.0),
                      ),
                    ),
                    onPressed: () => _showMaterialsSelector(context),
                    icon: const Icon(Icons.open_in_new, size: 16),
                    label: const Text('Select'),
                  ),
                ],
              ),
              if (appState.selectedMaterials.isEmpty)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 20.0),
                  child: Text(
                    'No materials selected yet. Add Tracking cards above.',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 13, color: Colors.grey),
                  ),
                ),
            ],
          ),
        ),

        // Inline Material Forms List
        ...appState.selectedMaterials.map((m) {
          return MaterialFormWidget(materialName: m);
        }).toList(),

        const SizedBox(height: 20),

        // Section 4: Actions Bar
        Row(
          children: [
            Expanded(
              child: SizedBox(
                height: 48,
                child: ElevatedButton.icon(
                  onPressed: () async {
                    final isSaved = await appState.saveEntry();
                    if (isSaved) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Entry saved successfully!')),
                      );
                    } else {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Please verify checker, block and quantities.'),
                          backgroundColor: Colors.red,
                        ),
                      );
                    }
                  },
                  icon: const Icon(Icons.save),
                  label: const Text('Save Entry'),
                ),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: SizedBox(
                height: 48,
                child: ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF25D366),
                    foregroundColor: Colors.white,
                  ),
                  onPressed: () async {
                    final isSaved = await appState.saveEntry();
                    if (isSaved) {
                      // Get last saved entry
                      if (appState.entries.isNotEmpty) {
                        final entry = appState.entries.first;
                        await ExportService.shareViaWhatsApp(entry);
                      }
                    } else {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Verification failed. Fill fields correctly.'),
                          backgroundColor: Colors.red,
                        ),
                      );
                    }
                  },
                  icon: const Icon(Icons.share),
                  label: const Text('Save & WhatsApp'),
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        OutlinedButton.icon(
          onPressed: () {
            appState.clearForm();
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Form cleared')),
            );
          },
          icon: const Icon(Icons.clear_all),
          label: const Text('Clear Form'),
        ),
        const SizedBox(height: 32),
      ],
    );
  }
}
