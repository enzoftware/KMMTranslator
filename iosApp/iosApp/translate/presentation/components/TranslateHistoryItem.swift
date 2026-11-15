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
    var isPostcardShelf: Bool = false
    var onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 12) {
                HStack(spacing: 6) {
                    HStack(spacing: 5) {
                        SmallLanguageIcon(language: item.fromLanguage, size: 18)

                        Image(systemName: "arrow.right")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundStyle(CozyTheme.Colors.inkMuted)

                        SmallLanguageIcon(language: item.toLanguage, size: 18)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 5)
                    .background(
                        Capsule()
                            .fill(CozyTheme.Colors.paperMuted.opacity(0.65))
                    )

                    Text("\(item.fromLanguage.language.languageName) → \(item.toLanguage.language.languageName)")
                        .font(.caption2.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                        .lineLimit(1)

                    Spacer(minLength: 4)

                    Image(systemName: "arrow.up.backward")
                        .font(.caption2.weight(.bold))
                        .foregroundStyle(CozyTheme.Colors.indigo)
                        .frame(width: 24, height: 24)
                        .background(Circle().fill(CozyTheme.Colors.indigoSoft))
                }

                VStack(alignment: .leading, spacing: 6) {
                    Text(item.toText)
                        .font(.system(.headline, design: .serif, weight: .semibold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .lineLimit(isPostcardShelf ? 2 : 3)
                        .multilineTextAlignment(.leading)

                    Text(item.fromText)
                        .font(.caption)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                }

                if isPostcardShelf {
                    Spacer(minLength: 0)
                }
            }
            .frame(
                width: isPostcardShelf ? 252 : nil,
                alignment: .topLeading
            )
            .frame(
                maxWidth: isPostcardShelf ? nil : .infinity,
                minHeight: isPostcardShelf ? 132 : nil,
                alignment: .topLeading
            )
            .padding(CozyTheme.Spacing.md)
            .cozyCard(cornerRadius: 22)
        }
        .buttonStyle(CozyScaleButtonStyle())
    }
}

#Preview("Postcard & List") {
    VStack(spacing: 16) {
        TranslateHistoryItem(
            item: UiHistoryItem(
                id: 0,
                fromText: "Break a leg tonight!",
                toText: "¡Mucha suerte esta noche!",
                fromLanguage: UiLanguage(language: .english, imagePath: "english"),
                toLanguage: UiLanguage(language: .spanish, imagePath: "spanish")
            ),
            isPostcardShelf: true,
            onClick: {}
        )

        TranslateHistoryItem(
            item: UiHistoryItem(
                id: 1,
                fromText: "Hello, good morning!",
                toText: "Hola, ¡buenos días!",
                fromLanguage: UiLanguage(language: .english, imagePath: "english"),
                toLanguage: UiLanguage(language: .spanish, imagePath: "spanish")
            ),
            onClick: {}
        )
    }
    .padding()
    .background(CozyAmbientBackground())
}
