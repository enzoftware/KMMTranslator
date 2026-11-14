import Foundation
import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct SettingsStoreTests {

    private func makeIsolatedDefaults() -> (UserDefaults, String) {
        let suiteName = "SettingsStoreTests.\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: suiteName)!
        defaults.removePersistentDomain(forName: suiteName)
        return (defaults, suiteName)
    }

    @Test func initialStateUsesDefaultsWhenStorageEmpty() {
        let (defaults, suiteName) = makeIsolatedDefaults()
        defer { defaults.removePersistentDomain(forName: suiteName) }

        let store = SettingsStore(userDefaults: defaults)

        #expect(store.userName == "")
        #expect(store.trimmedUserName == "")
        #expect(store.initials == "Y")
        #expect(store.theme == .light)
        #expect(store.isDarkMode == false)
        #expect(store.versionInfo.appName == "Yapper")
        #expect(!store.versionInfo.displayVersion.isEmpty)
    }

    @Test func userNamePersistsAcrossInstancesAndComputesInitials() {
        let (defaults, suiteName) = makeIsolatedDefaults()
        defer { defaults.removePersistentDomain(forName: suiteName) }

        let store = SettingsStore(userDefaults: defaults)
        store.userName = "  Enzo Lizama  "

        #expect(store.trimmedUserName == "Enzo Lizama")
        #expect(store.initials == "EL")

        let reloaded = SettingsStore(userDefaults: defaults)
        #expect(reloaded.userName == "  Enzo Lizama  ")
        #expect(reloaded.trimmedUserName == "Enzo Lizama")
    }

    @Test func themeToggleAndSelectionPersistAcrossAppRestarts() {
        let (defaults, suiteName) = makeIsolatedDefaults()
        defer { defaults.removePersistentDomain(forName: suiteName) }

        let firstLaunch = SettingsStore(userDefaults: defaults)
        #expect(firstLaunch.theme == .light)

        firstLaunch.toggleTheme()
        #expect(firstLaunch.theme == .dark)
        #expect(firstLaunch.isDarkMode == true)
        #expect(firstLaunch.theme.colorScheme == .dark)

        let secondLaunch = SettingsStore(userDefaults: defaults)
        #expect(secondLaunch.theme == .dark)
        #expect(secondLaunch.isDarkMode == true)

        secondLaunch.isDarkMode = false
        let thirdLaunch = SettingsStore(userDefaults: defaults)
        #expect(thirdLaunch.theme == .light)
        #expect(thirdLaunch.theme.colorScheme == .light)
    }

    @Test func appVersionInfoFormatsVersionAndBuildCleanly() {
        let versionWithBuild = AppVersionInfo(appName: "Yapper", shortVersion: "2.1", buildNumber: "42")
        let matchingBuild = AppVersionInfo(appName: "Yapper", shortVersion: "1.0", buildNumber: "1.0")
        let emptyBuild = AppVersionInfo(appName: "Yapper", shortVersion: "1.0", buildNumber: "")

        #expect(versionWithBuild.displayVersion == "2.1 (42)")
        #expect(matchingBuild.displayVersion == "1.0")
        #expect(emptyBuild.displayVersion == "1.0")
    }

    @Test func settingsScreenRendersWithEnvironmentStore() {
        let (defaults, suiteName) = makeIsolatedDefaults()
        defer { defaults.removePersistentDomain(forName: suiteName) }

        let store = SettingsStore(userDefaults: defaults)
        store.userName = "Enzo"
        store.theme = .dark

        let host = UIHostingController(
            rootView: NavigationStack {
                SettingsScreen()
            }
            .environmentObject(store)
        )
        host.view.frame = CGRect(x: 0, y: 0, width: 390, height: 844)
        host.view.layoutIfNeeded()

        #expect(host.view.bounds.size == CGSize(width: 390, height: 844))
    }
}
