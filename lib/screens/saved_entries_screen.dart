import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/app_state.dart';
import '../services/export_service.dart';
import '../models/models.dart';

class SavedEntriesScreen extends StatelessWidget {
  const SavedEntriesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);
    final search = appState.searchQuery;

    final filtered = appState.entries.where((e) {
      return e.checker.toLowerCase().contains(search.toLowerCase()) ||
          e.tm.toLowerCase().contains(search.toLowerCase()) ||
          e.block.toLowerCase().contains(search.toLowerCase()) ||
          e.subBlock.toLowerCase().contains(search.toLowerCase()) ||
          e.drrCode.toLowerCase().contains(search.toLowerCase()) ||
          e.materials.any((m) => m.name.toLowerCase().contains(search.toLowerCase()));
    }).toList();

    return Column(
      children: [
        // Search Filter TextField
        Padding(
          padding: const EdgeInsets.all(12.0),
          child: TextField(
            onChanged: (val) {
              appState.setSearchQuery(val);
            },
            decoration: InputDecoration(
              labelText: 'Search historical records...',
              prefixIcon: const Icon(Icons.search),
              suffixIcon: search.isNotEmpty
                  ? IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () {
                        appState.setSearchQuery('');
                      },
                    )
                  : null,
            ),
          ),
        ),

        // Bulk Export buttons
        if (appState.entries.isNotEmpty)
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12.0),
            child: Column(
              children: [
                Row(
                  children: [
                    Expanded(
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.green,
                          foregroundColor: Colors.white,
                        ),
                        onPressed: () => ExportService.exportXlsxSheet(appState.entries),
                        icon: const Icon(Icons.table_chart, size: 16),
                        label: const Text('Excel XLSX', style: TextStyle(fontSize: 11)),
                      ),
                    ),
                    const SizedBox(width: 4),
                    Expanded(
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.blue,
                          foregroundColor: Colors.white,
                        ),
                        onPressed: () => ExportService.exportCsvSheet(appState.entries),
                        icon: const Icon(Icons.description, size: 16),
                        label: const Text('Export CSV', style: TextStyle(fontSize: 11)),
                      ),
                    ),
                    const SizedBox(width: 4),
                    Expanded(
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.teal,
                          foregroundColor: Colors.white,
                        ),
                        onPressed: () => ExportService.exportJsonSheet(appState.entries),
                        icon: const Icon(Icons.code, size: 16),
                        label: const Text('Export JSON', style: TextStyle(fontSize: 11)),
                      ),
                    ),
                  ],
                ),
                SizedBox(
                  width: double.infinity,
                  child: OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      side: const BorderSide(color: Colors.red),
                      foregroundColor: Colors.red,
                    ),
                    onPressed: () {
                      showDialog(
                        context: context,
                        builder: (ctx) => AlertDialog(
                          title: const Text('Delete All Records'),
                          content: const Text(
                              'Are you absolutely sure you want to delete all entries? This action cannot be undone.'),
                          actions: [
                            TextButton(
                              onPressed: () => Navigator.pop(ctx),
                              child: const Text('Cancel'),
                            ),
                            TextButton(
                              onPressed: () {
                                appState.deleteAllEntries();
                                Navigator.pop(ctx);
                              },
                              child: const Text('Delete All', style: TextStyle(color: Colors.red)),
                            ),
                          ],
                        ),
                      );
                    },
                    icon: const Icon(Icons.delete_forever, size: 18),
                    label: const Text('Delete All Entries'),
                  ),
                ),
                const Divider(),
              ],
            ),
          ),

        // History items scroll view
        Expanded(
          child: filtered.isEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.inventory_2,
                        size: 64,
                        color: Colors.grey.withOpacity(0.5),
                      ),
                      const SizedBox(height: 12),
                      Text(
                        search.isNotEmpty
                            ? 'No records match search filter.'
                            : 'No records tracked yet.',
                        style: const TextStyle(color: Colors.grey),
                      )
                    ],
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 4.0),
                  itemCount: filtered.length,
                  itemBuilder: (context, index) {
                    final entry = filtered[index];
                    return _HistoryCard(entry: entry);
                  },
                ),
        )
      ],
    );
  }
}

class _HistoryCard extends StatefulWidget {
  final FieldEntry entry;

  const _HistoryCard({required this.entry});

  @override
  State<_HistoryCard> createState() => _HistoryCardState();
}

class _HistoryCardState extends State<_HistoryCard> {
  bool _isExpanded = false;

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context, listen: false);
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 8.0),
      child: Column(
        children: [
          ListTile(
            title: Text(
              '${widget.entry.block} - ${widget.entry.subBlock} | TM: ${widget.entry.tm}',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Checker: ${widget.entry.checker}', style: const TextStyle(fontSize: 13)),
                Text(widget.entry.date, style: TextStyle(fontSize: 11, color: theme.colorScheme.outline)),
              ],
            ),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: const Icon(Icons.share, color: Color(0xFF25D366)),
                  onPressed: () => ExportService.shareViaWhatsApp(widget.entry),
                ),
                IconButton(
                  icon: Icon(Icons.delete, color: theme.colorScheme.error),
                  onPressed: () {
                    showDialog(
                      context: context,
                      builder: (ctx) => AlertDialog(
                        title: const Text('Delete Entry'),
                        content: const Text('Are you sure you want to delete this tracked record?'),
                        actions: [
                          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
                          TextButton(
                            onPressed: () {
                              appState.deleteEntry(widget.entry.id);
                              Navigator.pop(ctx);
                            },
                            child: const Text('Delete', style: TextStyle(color: Colors.red)),
                          ),
                        ],
                      ),
                    );
                  },
                ),
              ],
            ),
            onTap: () {
              setState(() {
                _isExpanded = !_isExpanded;
              });
            },
          ),
          if (_isExpanded)
            Padding(
              padding: const EdgeInsets.only(left: 16.0, right: 16.0, bottom: 16.0),
              child: Align(
                alignment: Alignment.topLeft,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Divider(),
                    if (widget.entry.mrfNumber.isNotEmpty || widget.entry.drrCode.isNotEmpty) ...[
                      Row(
                        children: [
                          if (widget.entry.mrfNumber.isNotEmpty)
                            Text(
                              'MRF: ${widget.entry.mrfNumber}   ',
                              style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
                            ),
                          if (widget.entry.drrCode.isNotEmpty)
                            Text(
                              'DRR: ${widget.entry.drrCode}',
                              style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: theme.colorScheme.secondary),
                            ),
                        ],
                      ),
                      const SizedBox(height: 6),
                    ],
                    if (widget.entry.schemes.isNotEmpty) ...[
                      Text(
                        'Schemes: ${widget.entry.schemes.join(', ')}',
                        style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurfaceVariant),
                      ),
                      const SizedBox(height: 10),
                    ],
                    const Text(
                      'Tracked Materials:',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 4),
                    ...widget.entry.materials.map((mat) {
                      return Card(
                        color: theme.colorScheme.surfaceVariant.withOpacity(0.4),
                        elevation: 0,
                        margin: const EdgeInsets.symmetric(vertical: 2.0),
                        child: Padding(
                          padding: const EdgeInsets.all(8.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(mat.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                              Row(
                                children: [
                                  if (mat.requested.isNotEmpty) Text('Req: ${mat.requested}  ', style: const TextStyle(fontSize: 11)),
                                  if (mat.received.isNotEmpty) Text('Rec: ${mat.received}  ', style: const TextStyle(fontSize: 11)),
                                  if (mat.used.isNotEmpty) Text('Used: ${mat.used}  ', style: const TextStyle(fontSize: 11)),
                                  if (mat.remaining.isNotEmpty) ...[
                                    Text(
                                      'Rem: ${mat.remaining}',
                                      style: TextStyle(
                                        fontSize: 11,
                                        fontWeight: FontWeight.bold,
                                        color: (double.tryParse(mat.remaining) ?? 0.0) < 0.0 ? Colors.red : theme.colorScheme.primary,
                                      ),
                                    )
                                  ]
                                ],
                              ),
                              if (mat.comment.isNotEmpty)
                                Text('Note: ${mat.comment}', style: TextStyle(fontSize: 11, color: theme.colorScheme.outline)),
                            ],
                          ),
                        ),
                      );
                    }).toList(),
                  ],
                ),
              ),
            )
        ],
      ),
    );
  }
}
