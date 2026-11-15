//
//  CozyAmbientBackground.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

struct CozyAmbientBackground: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @Environment(\.colorScheme) private var colorScheme
    @State private var driftPhase = false

    static func meshPoints(drifted: Bool) -> [SIMD2<Float>] {
        let shift: Float = drifted ? 0.08 : -0.06
        return [
            .init(0.0, 0.0), .init(0.5 + shift * 0.5, 0.0), .init(1.0, 0.0),
            .init(0.0, 0.48 - shift * 0.4), .init(0.52 + shift, 0.46 - shift * 0.6), .init(1.0, 0.52 + shift * 0.4),
            .init(0.0, 1.0), .init(0.48 - shift * 0.5, 1.0), .init(1.0, 1.0)
        ]
    }

    var body: some View {
        ZStack {
            CozyTheme.Colors.paperBackground

            MeshGradient(
                width: 3,
                height: 3,
                points: Self.meshPoints(drifted: driftPhase),
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
            updateDriftAnimation(for: reduceMotion)
        }
        .onChange(of: reduceMotion) { _, newValue in
            updateDriftAnimation(for: newValue)
        }
    }

    private func updateDriftAnimation(for isReduceMotionEnabled: Bool) {
        if isReduceMotionEnabled {
            withAnimation(.default) {
                driftPhase = false
            }
        } else if !driftPhase {
            withAnimation(.easeInOut(duration: 9.0).repeatForever(autoreverses: true)) {
                driftPhase = true
            }
        }
    }
}

#Preview("CozyAmbientBackground") {
    ZStack {
        CozyAmbientBackground()
        Text("Warm Paper Mesh")
            .font(.headline)
            .foregroundStyle(CozyTheme.Colors.inkPrimary)
    }
}
