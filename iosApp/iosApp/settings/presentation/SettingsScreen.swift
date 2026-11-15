//
//  SettingsScreen.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

struct SettingsScreen: View {
    @EnvironmentObject private var settingsStore: SettingsStore
    @Environment(\.dismiss) private var dismiss
    @FocusState private var isNameFocused: Bool

    var body: some View {
        ZStack {
            CozyAmbientBackground()

            ScrollView {
                VStack(alignment: .leading, spacing: CozyTheme.Spacing.lg) {
                    topBar
                        .cozyEntrance(delay: 0.0)

                    profileSection
                        .cozyEntrance(delay: 0.05)

                    appearanceSection
                        .cozyEntrance(delay: 0.1)

                    aboutSection
                        .cozyEntrance(delay: 0.15)
                }
                .padding(.horizontal, CozyTheme.Spacing.md)
                .padding(.top, CozyTheme.Spacing.sm)
                .padding(.bottom, CozyTheme.Spacing.xl)
            }
            .scrollIndicators(.hidden)
            .scrollDismissesKeyboard(.interactively)
        }
        .toolbar(.hidden, for: .navigationBar)
    }

    private var topBar: some View {
        HStack(spacing: 12) {
            CozyGlassContainer(spacing: 10) {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.subheadline.weight(.bold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .frame(width: 40, height: 40)
                        .cozyFloatingChrome(in: Circle(), interactive: true)
                }
                .buttonStyle(CozyScaleButtonStyle())
                .accessibilityLabel("Back to translator")
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("Settings")
                    .font(.system(.title2, design: .rounded, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)

                Text("Personalize your cozy space")
                    .font(.caption)
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)
            }

            Spacer()
        }
    }

    private var profileSection: some View {
        VStack(alignment: .leading, spacing: CozyTheme.Spacing.md) {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(CozyTheme.Colors.indigoSoft)
                        .frame(width: 52, height: 52)

                    Text(settingsStore.initials)
                        .font(.system(.title3, design: .rounded, weight: .bold))
                        .foregroundStyle(CozyTheme.Colors.indigo)
                        .contentTransition(.numericText())
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text(
                        settingsStore.trimmedUserName.isEmpty
                            ? "Word Traveler"
                            : "Hi, \(settingsStore.trimmedUserName)!"
                    )
                    .font(.system(.headline, design: .rounded, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    .contentTransition(.interpolate)

                    Text("Your name appears on the home header")
                        .font(.caption)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                }

                Spacer()
            }

            HStack(spacing: 10) {
                Image(systemName: "person.fill")
                    .font(.subheadline)
                    .foregroundStyle(CozyTheme.Colors.indigo)

                TextField(
                    "Your name",
                    text: $settingsStore.userName,
                    prompt: Text("Enter your name")
                        .foregroundStyle(CozyTheme.Colors.inkMuted)
                )
                .font(.body)
                .foregroundStyle(CozyTheme.Colors.inkPrimary)
                .focused($isNameFocused)
                .submitLabel(.done)

                if !settingsStore.userName.isEmpty {
                    Button {
                        withAnimation(CozyTheme.Motion.snappySpring) {
                            settingsStore.userName = ""
                        }
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.subheadline)
                            .foregroundStyle(CozyTheme.Colors.inkMuted)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("Clear name")
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .background(
                RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous)
                    .fill(CozyTheme.Colors.paperMuted.opacity(0.55))
            )
            .overlay(
                RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous)
                    .strokeBorder(
                        isNameFocused ? CozyTheme.Colors.indigo.opacity(0.6) : CozyTheme.Colors.borderSubtle,
                        lineWidth: 1
                    )
            )
        }
        .padding(CozyTheme.Spacing.md)
        .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)
    }

    private var appearanceSection: some View {
        VStack(alignment: .leading, spacing: CozyTheme.Spacing.md) {
            VStack(alignment: .leading, spacing: 3) {
                Label("Appearance", systemImage: "paintpalette.fill")
                    .font(.system(.headline, design: .rounded, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)

                Text("Pick your favorite paper mood")
                    .font(.caption)
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)
            }

            HStack(spacing: CozyTheme.Spacing.sm) {
                ForEach(AppTheme.allCases) { theme in
                    themeOptionCard(for: theme)
                }
            }
        }
        .padding(CozyTheme.Spacing.md)
        .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)
        .sensoryFeedback(.selection, trigger: settingsStore.theme)
    }

    private func themeOptionCard(for theme: AppTheme) -> some View {
        let isSelected = settingsStore.theme == theme
        let previewPaper = theme == .light ? Color(hex: 0xFFFA_F5EC) : Color(hex: 0xFF17_1412)
        let previewCard = theme == .light ? Color(hex: 0xFFFF_FDF8) : Color(hex: 0xFF2B_2622)
        let previewAccent = theme == .light ? Color(hex: 0xFF4B_4DED) : Color(hex: 0xFF7D_80F9)

        return Button {
            withAnimation(CozyTheme.Motion.cozySpring) {
                settingsStore.theme = theme
            }
        } label: {
            VStack(alignment: .leading, spacing: 10) {
                ZStack(alignment: .topTrailing) {
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .fill(previewPaper)
                        .frame(height: 62)
                        .overlay(alignment: .bottomLeading) {
                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                .fill(previewCard)
                                .frame(height: 28)
                                .overlay(alignment: .leading) {
                                    Circle()
                                        .fill(previewAccent)
                                        .frame(width: 10, height: 10)
                                        .padding(.leading, 8)
                                }
                                .padding(8)
                        }
                        .overlay(
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .strokeBorder(CozyTheme.Colors.borderSubtle, lineWidth: 1)
                        )

                    Image(systemName: isSelected ? "checkmark.circle.fill" : theme.iconName)
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(isSelected ? CozyTheme.Colors.indigo : previewAccent)
                        .padding(6)
                        .contentTransition(.symbolEffect(.replace))
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(theme.title)
                        .font(.subheadline.weight(.bold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)

                    Text(theme.subtitle)
                        .font(.caption)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                }
            }
            .padding(10)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous)
                    .fill(
                        isSelected
                            ? CozyTheme.Colors.indigoSoft.opacity(0.65)
                            : CozyTheme.Colors.paperMuted.opacity(0.4)
                    )
            )
            .overlay(
                RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous)
                    .strokeBorder(
                        isSelected ? CozyTheme.Colors.indigo : CozyTheme.Colors.borderSubtle,
                        lineWidth: isSelected ? 1.5 : 1
                    )
            )
        }
        .buttonStyle(CozyScaleButtonStyle())
        .accessibilityLabel("\(theme.title) theme")
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }

    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: CozyTheme.Spacing.sm) {
            Label("About", systemImage: "sparkles")
                .font(.system(.headline, design: .rounded, weight: .bold))
                .foregroundStyle(CozyTheme.Colors.inkPrimary)

            HStack {
                Text("App Name")
                    .font(.subheadline)
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)

                Spacer()

                Text(settingsStore.versionInfo.appName)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
            }
            .padding(.top, 4)

            Divider()
                .overlay(CozyTheme.Colors.borderSubtle)

            HStack {
                Text("Version")
                    .font(.subheadline)
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)

                Spacer()

                Text("v\(settingsStore.versionInfo.displayVersion)")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(CozyTheme.Colors.indigo)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(
                        Capsule()
                            .fill(CozyTheme.Colors.indigoSoft)
                    )
            }
        }
        .padding(CozyTheme.Spacing.md)
        .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)
    }
}

#Preview("SettingsScreen") {
    let suiteName = "SettingsScreenPreview"
    let defaults = UserDefaults(suiteName: suiteName) ?? .standard
    let store = SettingsStore(userDefaults: defaults)

    NavigationStack {
        SettingsScreen()
            .environmentObject(store)
    }
}
