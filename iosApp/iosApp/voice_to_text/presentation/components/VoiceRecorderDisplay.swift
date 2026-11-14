//
//  VoiceRecorderDisplay.swift
//  iosApp
//
//  Created by Enzo Lizama on 24/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI

struct VoiceRecorderDisplay: View {
    var powerRatios: [Double]

    var body: some View {
        Canvas { context, size in
            let rect = CGRect(origin: .zero, size: size)
            context.clip(to: Path(rect))

            let barWidth: CGFloat = 4.0
            let barSpacing: CGFloat = 4.0
            let stride = barWidth + barSpacing
            let barCount = max(1, Int(size.width / stride))
            let minLevel = 0.08

            let normalized = powerRatios
                .map { min(max(minLevel, $0), 1.0) }
                .suffix(barCount)
                .reversed()

            let gradient = GraphicsContext.Shading.linearGradient(
                Gradient(colors: [
                    CozyTheme.Colors.indigo,
                    CozyTheme.Colors.terracotta
                ]),
                startPoint: CGPoint(x: size.width * 0.5, y: 0),
                endPoint: CGPoint(x: size.width * 0.5, y: size.height)
            )

            let centerY = size.height / 2.0
            let maxHalfHeight = max(8.0, centerY - 12.0)

            for (index, ratio) in normalized.enumerated() {
                let edgeDistance = min(1.0, Double(barCount - index) / Double(max(barCount / 4, 1)))
                let taperedRatio = ratio * (0.45 + 0.55 * edgeDistance)
                let halfHeight = max(3.0, maxHalfHeight * taperedRatio)
                let x = size.width - barWidth - CGFloat(index) * stride
                let barRect = CGRect(
                    x: x,
                    y: centerY - halfHeight,
                    width: barWidth,
                    height: halfHeight * 2.0
                )
                let barPath = Path(roundedRect: barRect, cornerRadius: barWidth / 2.0)
                context.fill(barPath, with: gradient)
            }
        }
        .padding(.horizontal, CozyTheme.Spacing.md)
        .padding(.vertical, CozyTheme.Spacing.sm)
        .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)
        .animation(CozyTheme.Motion.snappySpring, value: powerRatios.count)
    }
}

#Preview {
    VoiceRecorderDisplay(
        powerRatios: [0.15, 0.35, 0.65, 0.9, 0.45, 0.8, 0.55, 0.3, 0.7, 0.95, 0.6, 0.4]
    )
    .frame(height: 120)
    .padding()
    .background(CozyAmbientBackground())
}
