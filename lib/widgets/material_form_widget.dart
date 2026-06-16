import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/app_state.dart';

class MaterialFormWidget extends StatelessWidget {
  final String materialName;

  const MaterialFormWidget({
    super.key,
    required this.materialName,
  });

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);
    final formState = appState.materialFormState[materialName] ?? FormFieldsState();

    final rec = double.tryParse(formState.received) ?? 0.0;
    final used = double.tryParse(formState.used) ?? 0.0;
    final usedExceedsReceived = used > rec;

    final theme = Theme.of(context);
    final highlightBg = usedExceedsReceived
        ? theme.colorScheme.errorContainer
        : theme.colorScheme.surfaceVariant;
    final highlightText = usedExceedsReceived
        ? theme.colorScheme.onErrorContainer
        : theme.colorScheme.onSurfaceVariant;

    return Card(
      margin: const EdgeInsets.symmetric(vertical: 6.0),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12.0),
        side: BorderSide(
          color: usedExceedsReceived ? theme.colorScheme.error : theme.colorScheme.outlineVariant,
          width: 1,
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Text(
                    materialName,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: usedExceedsReceived ? theme.colorScheme.error : theme.colorScheme.onSurface,
                    ),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close, size: 20),
                  onPressed: () {
                    appState.toggleMaterialSelected(materialName, false);
                  },
                ),
              ],
            ),
            const SizedBox(height: 8),

            // Requested, received, used grid
            Row(
              children: [
                Expanded(
                  child: TextFormField(
                    initialValue: formState.requested,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Req',
                      contentPadding: EdgeInsets.symmetric(horizontal: 8, vertical: 8),
                    ),
                    onChanged: (val) {
                      appState.updateMaterialField(materialName, requested: val);
                    },
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: TextFormField(
                    initialValue: formState.received,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Rec',
                      contentPadding: EdgeInsets.symmetric(horizontal: 8, vertical: 8),
                    ),
                    onChanged: (val) {
                      appState.updateMaterialField(materialName, received: val);
                    },
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: TextFormField(
                    initialValue: formState.used,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Used',
                      contentPadding: EdgeInsets.symmetric(horizontal: 8, vertical: 8),
                    ),
                    onChanged: (val) {
                      appState.updateMaterialField(materialName, used: val);
                    },
                  ),
                ),
                const SizedBox(width: 8),

                // Remaining (readonly & highlighting)
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
                    decoration: BoxDecoration(
                      color: highlightBg,
                      borderRadius: BorderRadius.circular(8.0),
                      border: BorderStroke(
                        usedExceedsReceived ? theme.colorScheme.error : theme.colorScheme.outline,
                        width: 1,
                      ) as BoxBorder?,
                    ),
                    child: Text(
                      formState.remaining.isEmpty ? '-' : formState.remaining,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.bold,
                        color: highlightText,
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),

            // Comments
            TextFormField(
              initialValue: formState.comment,
              decoration: const InputDecoration(
                labelText: 'Comment / Location Notes',
                contentPadding: EdgeInsets.symmetric(horizontal: 10, vertical: 10),
              ),
              onChanged: (val) {
                appState.updateMaterialField(materialName, comment: val);
              },
            ),
          ],
        ),
      ),
    );
  }

  static BoxBorder BorderStroke(Color color, {double width = 1}) {
    return Border.all(color: color, width: width);
  }
}
