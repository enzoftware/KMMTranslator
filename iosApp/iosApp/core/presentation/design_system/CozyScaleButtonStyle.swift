//
//  CozyScaleButtonStyle.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

struct CozyScaleButtonStyle: ButtonStyle {
    var pressedScale: CGFloat = 0.96

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? pressedScale : 1.0)
            .opacity(configuration.isPressed ? 0.92 : 1.0)
            .animation(CozyTheme.Motion.snappySpring, value: configuration.isPressed)
    }
}

#Preview("CozyScaleButtonStyle") {
    Button {
    } label: {
        Text("Tap & Hold Me")
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(CozyTheme.Colors.onIndigo)
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(Capsule().fill(CozyTheme.Colors.indigo))
    }
    .buttonStyle(CozyScaleButtonStyle())
    .padding(32)
    .background(CozyAmbientBackground())
}
