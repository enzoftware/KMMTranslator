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
            context.clip(to: Path(CGRect(origin: .zero, size: size)))

            let barWidth = 3.0
            let barCount = Int(size.width / Double(2 * barWidth))
            let defaultLevel = 0.05
            let reversedRatios = powerRatios.map {
                ratio in
                min(max(defaultLevel, ratio), 1.0)
            }
            .suffix(barCount)
            .reversed()
            for (index, powerRatio) in reversedRatios.enumerated() {
                let centerY = CGFloat(size.height / 2.0)
                let yTopStart = CGFloat(centerY - centerY * powerRatio)
                var path = Path()
                path.addRoundedRect(
                    in: CGRect(
                        x: CGFloat(size.width) - barWidth - CGFloat(index) * 2.0
                            * barWidth,
                        y: yTopStart,
                        width: barWidth,
                        height: (centerY - yTopStart) * 2.0
                    ),
                    cornerSize: CGSize(width: 10.0, height: 10.0)
                )
                context.fill(path, with: .color(.primaryColor))
            }

        }
        .gradientSurface()
        .cornerRadius(20)
        .padding(.horizontal, 15)
        .padding(.vertical, 5)
        .shadow(radius: 4)
    }
}

#Preview {
    VoiceRecorderDisplay(
        powerRatios: [1.0, 0.5, 0.3, 0.8, 0.2, 0.9, 0.4, 0.6, 0.1, 0.7]
    )
}
