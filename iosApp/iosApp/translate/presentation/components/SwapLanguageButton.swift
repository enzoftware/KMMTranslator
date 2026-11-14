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
    @State private var swapCount = 0

    var body: some View {
        Button {
            withAnimation(CozyTheme.Motion.playfulBounce) {
                swapCount += 1
            }
            onClick()
        } label: {
            ZStack {
                Circle()
                    .fill(CozyTheme.Colors.indigo)
                    .frame(width: 38, height: 38)
                    .shadow(
                        color: CozyTheme.Colors.indigo.opacity(0.3),
                        radius: 8,
                        x: 0,
                        y: 4
                    )

                Image(systemName: "arrow.left.arrow.right")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.onIndigo)
                    .rotationEffect(.degrees(Double(swapCount) * 180))
            }
        }
        .buttonStyle(CozyScaleButtonStyle(pressedScale: 0.9))
        .sensoryFeedback(.impact(flexibility: .soft), trigger: swapCount)
        .accessibilityLabel("Swap languages")
    }
}

#Preview {
    SwapLanguageButton(onClick: {})
        .padding()
}
