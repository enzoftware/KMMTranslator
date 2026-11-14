//
//  LanguageDropdownItem.swift
//  iosApp
//
//  Created by Enzo Lizama on 20/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import UIKit
import shared

struct LanguageDropdownItem: View {
    var language: UiLanguage
    var onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            Label {
                Text(language.language.languageName)
                    .foregroundStyle(.primary)
            } icon: {
                if let imagePath = language.imagePath?.lowercased(),
                   let image = UIImage(named: imagePath) {
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 24, height: 24)
                        .clipShape(Circle())
                } else {
                    Image(systemName: "globe")
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
