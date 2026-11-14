//
//  ProgressButton.swift
//  iosApp
//
//  Created by Enzo Lizama on 22/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ProgressButton: View {
    var text: String
    var isLoading: Bool
    var isDisabled: Bool = false
    var onClick: () -> Void

    var body: some View {
        Button {
            if !isLoading && !isDisabled {
                onClick()
            }
        } label: {
            HStack(spacing: 8) {
                if isLoading {
                    ProgressView()
                        .controlSize(.small)
                        .tint(CozyTheme.Colors.onIndigo)
                    Text("Translating")
                        .font(.subheadline.weight(.semibold))
                } else {
                    Text(text)
                        .font(.subheadline.weight(.semibold))
                    Image(systemName: "sparkles")
                        .font(.caption.weight(.bold))
                }
            }
            .foregroundStyle(CozyTheme.Colors.onIndigo)
            .padding(.horizontal, isLoading ? 16 : 18)
            .padding(.vertical, 10)
            .background(
                Capsule()
                    .fill(
                        isDisabled && !isLoading
                            ? CozyTheme.Colors.indigo.opacity(0.45)
                            : CozyTheme.Colors.indigo
                    )
            )
            .shadow(
                color: CozyTheme.Colors.indigo.opacity(isDisabled ? 0.08 : 0.28),
                radius: 10,
                x: 0,
                y: 4
            )
            .animation(CozyTheme.Motion.snappySpring, value: isLoading)
            .animation(CozyTheme.Motion.gentleEase, value: isDisabled)
        }
        .buttonStyle(CozyScaleButtonStyle())
        .disabled(isDisabled && !isLoading)
        .sensoryFeedback(.impact(weight: .light), trigger: isLoading) { _, newValue in
            newValue
        }
    }
}

#Preview {
    VStack(spacing: 16) {
        ProgressButton(text: "Translate", isLoading: false, onClick: {})
        ProgressButton(text: "Translate", isLoading: true, onClick: {})
        ProgressButton(text: "Translate", isLoading: false, isDisabled: true, onClick: {})
    }
    .padding()
}
