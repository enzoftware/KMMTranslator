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
        Button(action: onClick){
            VStack(alignment: .leading){
                HStack {
                    SmallLanguageIcon(language: item.fromLanguage)
                        .padding(.trailing)
                    Text(item.fromText)
                        .foregroundColor(.lightBlue)
                        .font(.body)
                    
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                
                HStack {
                    SmallLanguageIcon(language: item.toLanguage)
                        .padding(.trailing)
                    Text(item.toText)
                        .foregroundColor(.onSurface)
                        .font(.body.weight(.semibold))
                    
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .frame(maxWidth: .infinity)
            .padding()
            .gradientSurface()
            .cornerRadius(15)
            .shadow(radius: 4)
        }
    }
}

#Preview {
    TranslateHistoryItem(
        item: UiHistoryItem(
            id: 0,
            fromText: "Hello",
            toText: "Hola",
            fromLanguage: UiLanguage(language: .english, imagePath: "english"),
            toLanguage: UiLanguage(language: .spanish, imagePath: "spanish")),
            onClick: {
                
            }
    )
}
