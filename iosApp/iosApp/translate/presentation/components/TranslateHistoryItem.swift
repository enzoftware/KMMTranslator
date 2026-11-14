//
//  TranslateHistoryItem.swift
//  iosApp
//
//  Created by Enzo Lizama on 23/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct TranslateHistoryItem: View {
    var item: UiHistoryItem
    var onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 8) {
                    SmallLanguageIcon(language: item.fromLanguage, size: 22)

                    Image(systemName: "arrow.right")
                        .font(.caption2.weight(.bold))
                        .foregroundStyle(CozyTheme.Colors.inkMuted)

                    SmallLanguageIcon(language: item.toLanguage, size: 22)

                    Text("\(item.fromLanguage.language.languageName) → \(item.toLanguage.language.languageName)")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)

                    Spacer()

                    Image(systemName: "arrow.up.backward.circle")
                        .font(.subheadline)
                        .foregroundStyle(CozyTheme.Colors.inkMuted)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text(item.fromText)
                        .font(.subheadline)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)

                    Text(item.toText)
                        .font(.body.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .lineLimit(3)
                        .multilineTextAlignment(.leading)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(CozyTheme.Spacing.md)
            .cozyCard(cornerRadius: 20)
        }
        .buttonStyle(CozyScaleButtonStyle())
    }
}

#Preview {
    TranslateHistoryItem(
        item: UiHistoryItem(
            id: 0,
            fromText: "Hello, good morning!",
            toText: "Hola, ¡buenos días!",
            fromLanguage: UiLanguage(language: .english, imagePath: "english"),
            toLanguage: UiLanguage(language: .spanish, imagePath: "spanish")
        ),
        onClick: {}
    )
    .padding()
    .background(CozyAmbientBackground())
}
