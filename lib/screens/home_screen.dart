import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/app_state.dart';
import 'entry_form_screen.dart';
import 'saved_entries_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);

    final viewScreens = <Widget>[
      const EntryFormScreen(),
      const SavedEntriesScreen(),
    ];

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Icon(
              Icons.construction,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(width: 8),
            const Text(
              'Field Material App',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: Icon(appState.isDarkMode ? Icons.light_mode : Icons.dark_mode),
            onPressed: () {
              appState.toggleDarkMode();
            },
          )
        ],
        elevation: 1,
        backgroundColor: Theme.of(context).colorScheme.surface,
      ),
      body: SafeArea(
        child: viewScreens[appState.currentTab],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: appState.currentTab,
        onDestinationSelected: (idx) {
          appState.setTab(idx);
        },
        destinations: [
          const NavigationDestination(
            icon: Icon(Icons.add_circle),
            label: 'New Entry',
          ),
          NavigationDestination(
            icon: Badge(
              label: Text(appState.entries.length.toString()),
              isLabelVisible: appState.entries.isNotEmpty,
              child: const Icon(Icons.assignment),
            ),
            label: 'Saved Entries',
          ),
        ],
      ),
    );
  }
}
