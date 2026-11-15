import SwiftUI
import shared

struct ContentView: View {
    private let appModule = AppModule()

    var body: some View {
        ZStack {
            CozyAmbientBackground()
            TranslateScreen(
                historyDataSource: appModule.historyDataSource,
                savedWordsDataSource: appModule.savedWordsDataSource,
                translateUseCase: appModule.translateUseCase
            )
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        let store = SettingsStore(
            userDefaults: UserDefaults(suiteName: "ContentViewPreview") ?? .standard
        )
        NavigationStack {
            ContentView()
        }
        .environmentObject(store)
        .preferredColorScheme(store.theme.colorScheme)
    }
}
