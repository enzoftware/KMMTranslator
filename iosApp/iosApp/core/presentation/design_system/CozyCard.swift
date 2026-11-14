//
//  CozyCard.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

struct CozyCardModifier: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme
    let cornerRadius: CGFloat
    let isElevated: Bool

    func body(content: Content) -> some View {
        let shape = RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
        let fillColor = isElevated ? CozyTheme.Colors.paperElevated : CozyTheme.Colors.paperCard

        content
            .background(
                shape.fill(fillColor)
            )
            .overlay(
                shape
                    .strokeBorder(
                        CozyTheme.Colors.borderSubtle.opacity(colorScheme == .dark ? 0.75 : 0.9),
                        lineWidth: 1
                    )
            )
            .clipShape(shape)
            .shadow(
                color: CozyTheme.Colors.warmShadow.opacity(colorScheme == .dark ? 0.36 : 0.07),
                radius: isElevated ? 18 : 10,
                x: 0,
                y: isElevated ? 8 : 4
            )
            .shadow(
                color: CozyTheme.Colors.warmShadow.opacity(colorScheme == .dark ? 0.18 : 0.03),
                radius: 2,
                x: 0,
                y: 1
            )
    }
}

extension View {
    func cozyCard(
        cornerRadius: CGFloat = CozyTheme.Radius.card,
        isElevated: Bool = false
    ) -> some View {
        modifier(CozyCardModifier(cornerRadius: cornerRadius, isElevated: isElevated))
    }
}

#Preview("CozyCard") {
    VStack(spacing: 16) {
        Text("Standard Paper Card")
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()
            .cozyCard()

        Text("Elevated Paper Card")
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()
            .cozyCard(isElevated: true)
    }
    .padding()
    .background(CozyAmbientBackground())
}
