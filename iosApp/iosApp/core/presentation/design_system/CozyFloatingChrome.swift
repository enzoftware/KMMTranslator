//
//  CozyFloatingChrome.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

struct CozyFloatingChromeModifier<S: InsettableShape>: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme
    let shape: S
    let tint: Color?
    let interactive: Bool

    @ViewBuilder
    func body(content: Content) -> some View {
        if #available(iOS 26.0, *) {
            content
                .glassEffect(resolvedGlass, in: shape)
        } else {
            let tintOverlay = (tint ?? CozyTheme.Colors.paperCard)
                .opacity(tint == nil ? (colorScheme == .dark ? 0.42 : 0.58) : 0.88)
            let borderOpacity: Double = colorScheme == .dark ? 0.8 : 0.95

            content
                .background {
                    shape
                        .fill(.ultraThinMaterial)
                        .overlay {
                            shape.fill(tintOverlay)
                        }
                }
                .overlay {
                    shape.strokeBorder(
                        CozyTheme.Colors.borderSubtle.opacity(borderOpacity),
                        lineWidth: 1
                    )
                }
                .clipShape(shape)
                .shadow(
                    color: CozyTheme.Colors.warmShadow.opacity(colorScheme == .dark ? 0.35 : 0.1),
                    radius: 14,
                    x: 0,
                    y: 6
                )
        }
    }

    @available(iOS 26.0, *)
    private var resolvedGlass: Glass {
        var glass = Glass.regular
        if let tint {
            glass = glass.tint(tint)
        }
        if interactive {
            glass = glass.interactive()
        }
        return glass
    }
}

extension View {
    func cozyFloatingChrome<S: InsettableShape>(
        in shape: S,
        tint: Color? = nil,
        interactive: Bool = true
    ) -> some View {
        modifier(CozyFloatingChromeModifier(shape: shape, tint: tint, interactive: interactive))
    }
}

#Preview("CozyFloatingChrome") {
    VStack(spacing: 16) {
        Text("Floating Neutral Pill")
            .font(.subheadline.weight(.semibold))
            .padding(.horizontal, 18)
            .padding(.vertical, 12)
            .cozyFloatingChrome(in: Capsule())

        Text("Tinted Indigo Pill")
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(CozyTheme.Colors.onIndigo)
            .padding(.horizontal, 18)
            .padding(.vertical, 12)
            .cozyFloatingChrome(in: Capsule(), tint: CozyTheme.Colors.indigo)
    }
    .padding(32)
    .background(CozyAmbientBackground())
}
