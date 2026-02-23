//
//  SwapLanguageButton.swift
//  iosApp
//
//  Created by Enzo Lizama on 20/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//


import SwiftUI

struct SwapLanguageButton: View {
    
    var onClick: () -> Void
    
    var body: some View {
        Button(action: onClick){
            Image(uiImage: UIImage(named: "swap_languages")!)
                .padding()
                .background(Color.primaryColor)
                .clipShape(Circle())
        }
    }
}

#Preview {
    SwapLanguageButton(
        onClick: {
            
        }
    )
}
