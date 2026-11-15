import SwiftUI

@main
struct iOSApp: App {
    @StateObject private var settingsStore = SettingsStore()

    var body: some Scene {
        WindowGroup {
            NavigationStack {
                ContentView()
            }
            .environmentObject(settingsStore)
            .preferredColorScheme(settingsStore.theme.colorScheme)
        }
    }
}
