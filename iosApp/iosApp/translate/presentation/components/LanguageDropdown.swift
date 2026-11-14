//
//  LanguageDropdown.swift
//  iosApp
//
//  Created by Enzo Lizama on 20/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct LanguageDropdown: View {
    var language: UiLanguage
    var isOpen: Bool
    var selectedLanguage: (UiLanguage) -> Void

    var body: some View {
        Menu {
            ForEach(
                UiLanguage.Companion().allLanguages,
                id: \.self.language.code
            ) { item in
                LanguageDropdownItem(
                    language: item,
                    onClick: {
                        selectedLanguage(item)
                    }
                )
            }
        } label: {
            HStack(spacing: 8) {
                SmallLanguageIcon(language: language, size: 24)

                Text(language.language.languageName)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.85)
                    .id(language.language.code)
                    .transition(.blurReplace)

                Image(systemName: "chevron.down")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)
                    .rotationEffect(.degrees(isOpen ? 180 : 0))
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .frame(maxWidth: .infinity)
            .background(
                Capsule()
                    .fill(CozyTheme.Colors.paperMuted.opacity(0.65))
            )
            .animation(CozyTheme.Motion.snappySpring, value: language.language.code)
            .animation(CozyTheme.Motion.snappySpring, value: isOpen)
        }
        .buttonStyle(CozyScaleButtonStyle())
        .sensoryFeedback(.selection, trigger: language.language.code)
    }
}

#Preview {
    LanguageDropdown(
        language: UiLanguage(language: .german, imagePath: "german"),
        isOpen: false,
        selectedLanguage: { _ in }
    )
    .padding()
}
