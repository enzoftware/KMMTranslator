//
//  SettingsStore.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import Foundation
import SwiftUI

enum AppTheme: String, CaseIterable, Identifiable, Equatable {
    case light
    case dark

    var id: String { rawValue }

    var title: String {
        switch self {
        case .light: return "Light"
        case .dark: return "Dark"
        }
    }

    var subtitle: String {
        switch self {
        case .light: return "Warm Paper"
        case .dark: return "Espresso Ink"
        }
    }

    var iconName: String {
        switch self {
        case .light: return "sun.max.fill"
        case .dark: return "moon.stars.fill"
        }
    }

    var colorScheme: ColorScheme {
        switch self {
        case .light: return .light
        case .dark: return .dark
        }
    }

    var isDarkMode: Bool {
        self == .dark
    }
}

struct AppVersionInfo: Equatable {
    let appName: String
    let shortVersion: String
    let buildNumber: String

    init(bundle: Bundle = .main) {
        let info = bundle.infoDictionary
        self.appName = (info?["CFBundleDisplayName"] as? String)
            ?? (info?["CFBundleName"] as? String)
            ?? "Parley"
        self.shortVersion = (info?["CFBundleShortVersionString"] as? String) ?? "1.0"
        self.buildNumber = (info?["CFBundleVersion"] as? String) ?? "1"
    }

    init(appName: String, shortVersion: String, buildNumber: String) {
        self.appName = appName
        self.shortVersion = shortVersion
        self.buildNumber = buildNumber
    }

    var displayVersion: String {
        if buildNumber.isEmpty || buildNumber == shortVersion {
            return shortVersion
        }
        return "\(shortVersion) (\(buildNumber))"
    }
}

@MainActor
final class SettingsStore: ObservableObject {
    enum Keys {
        static let userName = "cozy.settings.userName"
        static let theme = "cozy.settings.theme"
    }

    private let userDefaults: UserDefaults

    @Published var userName: String {
        didSet {
            userDefaults.set(userName, forKey: Keys.userName)
        }
    }

    @Published var theme: AppTheme {
        didSet {
            userDefaults.set(theme.rawValue, forKey: Keys.theme)
        }
    }

    let versionInfo: AppVersionInfo

    init(
        userDefaults: UserDefaults = .standard,
        bundle: Bundle = .main,
        defaultTheme: AppTheme = .light
    ) {
        self.userDefaults = userDefaults
        self.versionInfo = AppVersionInfo(bundle: bundle)
        self.userName = userDefaults.string(forKey: Keys.userName) ?? ""

        if let rawTheme = userDefaults.string(forKey: Keys.theme),
           let storedTheme = AppTheme(rawValue: rawTheme) {
            self.theme = storedTheme
        } else {
            self.theme = defaultTheme
        }
    }

    var isDarkMode: Bool {
        get { theme == .dark }
        set { theme = newValue ? .dark : .light }
    }

    var trimmedUserName: String {
        userName.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    var initials: String {
        let components = trimmedUserName
            .split(separator: " ")
            .prefix(2)
            .compactMap(\.first)
        if components.isEmpty {
            return "P"
        }
        return String(components).uppercased()
    }

    func toggleTheme() {
        theme = theme == .dark ? .light : .dark
    }
}
