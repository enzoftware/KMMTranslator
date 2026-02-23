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
        HStack {
            SmallLanguageIcon(language: language)
                .padding(.trailing, 6)
            Text(language.language.name)
                .foregroundColor(.lightBlue)
        }

    }
}

#Preview {
    LanguageDisplay(
        language: UiLanguage(language: .english, imagePath: "english")
    )
}
