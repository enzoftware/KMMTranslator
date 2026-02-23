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
            VStack {
                ForEach(
                    UiLanguage.Companion().allLanguages,
                    id: \.self.language.code
                ) { language in
                    LanguageDropdownItem(
                        language: language,
                        onClick: {
                            selectedLanguage(language)
                        }
                    )
                }
            }
        } label: {
            HStack {
                SmallLanguageIcon(language: language)
                Text(language.language.languageName)
                    .foregroundColor(.lightBlue)
                Image(systemName: isOpen ? "chevron.up" : "chevron.down")
                    .foregroundColor(.lightBlue)
            }
        }
    }
}

#Preview {
    LanguageDropdown(
        language: UiLanguage(language: .german, imagePath: "german"),
        isOpen: true,
        selectedLanguage: { language in }
    )
}
