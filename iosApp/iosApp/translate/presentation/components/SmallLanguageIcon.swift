//
//  SmallLanguageIcon.swift
//  iosApp
//
//  Created by Enzo Lizama on 20/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct SmallLanguageIcon: View {
    var language: UiLanguage
    
    var body: some View {
        Image(uiImage: UIImage(named: language.imagePath?.lowercased() ?? "")!)
            .resizable()
            .frame(width: 30, height: 30)
    }
}

#Preview {
    SmallLanguageIcon(
        language: UiLanguage(language: .german, imagePath: "german")
    )
}
