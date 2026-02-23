//
//  LanguageDropdown.swift
//  iosApp
//
//  Created by Enzo Lizama on 20/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct LanguageDropdownItem: View {

    var language: UiLanguage
    var onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack {

                if let image = UIImage(
                    named: (language.imagePath?.lowercased())!
                ) {
                    Image(uiImage: image)
                        .resizable()
                        .frame(width: 40, height: 40)
                        .padding(.trailing, 6)
                    Text(language.language.languageName)
                        .foregroundColor(.textBlack)
                }

            }
        }
    }
}

#Preview {
    LanguageDropdownItem(
        language: UiLanguage(language: .german, imagePath: "german"),
        onClick: {}
    )
}
