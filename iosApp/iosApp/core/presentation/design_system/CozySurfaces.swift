//
//  CozySurfaces.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

struct CozyAmbientBackground: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @Environment(\.colorScheme) private var colorScheme
    @State private var driftPhase = false

    var body: some View {
        ZStack {
            CozyTheme.Colors.paperBackground

            MeshGradient(
                width: 3,
                height: 3,
                points: meshPoints,
                colors: [
                    CozyTheme.Colors.meshTopLeading,
                    CozyTheme.Colors.meshTopCenter,
                    CozyTheme.Colors.meshTopTrailing,
                    CozyTheme.Colors.meshTopLeading,
                    CozyTheme.Colors.meshMidCenter,
                    CozyTheme.Colors.meshBottomCool,
                    CozyTheme.Colors.meshBottomWarm,
                    CozyTheme.Colors.meshMidCenter,
                    CozyTheme.Colors.meshBottomCool
                ],
                smoothsColors: true
            )
            .opacity(colorScheme == .dark ? 0.92 : 1.0)
        }
        .ignoresSafeArea()
        .onAppear {
            guard !reduceMotion, !driftPhase else { return }
            withAnimation(.easeInOut(duration: 9.0).repeatForever(autoreverses: true)) {
                driftPhase = true
            }
        }
    }

    private var meshPoints: [SIMD2<Float>] {
        let shift: Float = driftPhase ? 0.08 : -0.06
        return [
            .init(0.0, 0.0), .init(0.5 + shift * 0.5, 0.0), .init(1.0, 0.0),
            .init(0.0, 0.48 - shift * 0.4), .init(0.52 + shift, 0.46 - shift * 0.6), .init(1.0, 0.52 + shift * 0.4),
            .init(0.0, 1.0), .init(0.48 - shift * 0.5, 1.0), .init(1.0, 1.0)
        ]
    }
}

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

struct CozyGlassContainer<Content: View>: View {
    let spacing: CGFloat
    @ViewBuilder let content: () -> Content

    init(spacing: CGFloat = 12, @ViewBuilder content: @escaping () -> Content) {
        self.spacing = spacing
        self.content = content
    }

    var body: some View {
        if #available(iOS 26.0, *) {
            GlassEffectContainer(spacing: spacing) {
                content()
            }
        } else {
            content()
        }
    }
}

struct CozyEntranceModifier: ViewModifier {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let delay: Double
    @State private var isVisible = false

    func body(content: Content) -> some View {
        content
            .opacity(isVisible || reduceMotion ? 1 : 0)
            .offset(y: isVisible || reduceMotion ? 0 : 14)
            .scaleEffect(isVisible || reduceMotion ? 1 : 0.975)
            .onAppear {
                guard !isVisible else { return }
                if reduceMotion {
                    isVisible = true
                } else {
                    withAnimation(CozyTheme.Motion.cozySpring.delay(delay)) {
                        isVisible = true
                    }
                }
            }
    }
}

extension View {
    func cozyCard(
        cornerRadius: CGFloat = CozyTheme.Radius.card,
        isElevated: Bool = false
    ) -> some View {
        modifier(CozyCardModifier(cornerRadius: cornerRadius, isElevated: isElevated))
    }

    func cozyFloatingChrome<S: InsettableShape>(
        in shape: S,
        tint: Color? = nil,
        interactive: Bool = true
    ) -> some View {
        modifier(CozyFloatingChromeModifier(shape: shape, tint: tint, interactive: interactive))
    }

    func cozyEntrance(delay: Double = 0) -> some View {
        modifier(CozyEntranceModifier(delay: delay))
    }
}
