//
//  SmallLanguageIcon.swift
//  iosApp
//
//  Created by Enzo Lizama on 20/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import UIKit
import shared

struct SmallLanguageIcon: View {
    var language: UiLanguage
    var size: CGFloat = 28

    var body: some View {
        Group {
            if let imagePath = language.imagePath?.lowercased(),
               let image = UIImage(named: imagePath) {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
            } else {
                ZStack {
                    CozyTheme.Colors.indigoSoft
                    Text(language.language.code.uppercased())
                        .font(.system(size: max(9, size * 0.36), weight: .bold, design: .rounded))
                        .foregroundStyle(CozyTheme.Colors.indigo)
                }
            }
        }
        .frame(width: size, height: size)
        .clipShape(Circle())
        .overlay(
            Circle()
                .strokeBorder(CozyTheme.Colors.borderSubtle, lineWidth: 1)
        )
    }
}

#Preview {
    HStack(spacing: 12) {
        SmallLanguageIcon(
            language: UiLanguage(language: .german, imagePath: "german")
        )
        SmallLanguageIcon(
            language: UiLanguage(language: .spanish, imagePath: nil),
            size: 24
        )
    }
    .padding()
}
