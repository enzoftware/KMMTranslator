//
//  LanguageDisplay.swift
//  iosApp
//
//  Created by Enzo Lizama on 22/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct LanguageDisplay: View {
    var language: UiLanguage

    var body: some View {
        HStack(spacing: 8) {
            SmallLanguageIcon(language: language, size: 22)
            Text(language.language.languageName)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(CozyTheme.Colors.inkSecondary)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 5)
        .background(
            Capsule()
                .fill(CozyTheme.Colors.paperMuted.opacity(0.65))
        )
    }
}

#Preview {
    LanguageDisplay(
        language: UiLanguage(language: .english, imagePath: "english")
    )
    .padding()
}
