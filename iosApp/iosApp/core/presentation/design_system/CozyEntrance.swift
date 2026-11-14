//
//  CozyEntrance.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

struct CozyEntranceModifier: ViewModifier {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let delay: Double
    let offsetY: CGFloat
    @State private var isVisible = false

    func body(content: Content) -> some View {
        content
            .opacity(isVisible || reduceMotion ? 1 : 0)
            .offset(y: (isVisible || reduceMotion) ? 0 : offsetY)
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
    func cozyEntrance(delay: Double = 0, offsetY: CGFloat = 14) -> some View {
        modifier(CozyEntranceModifier(delay: delay, offsetY: offsetY))
    }
}

#Preview("CozyEntrance") {
    VStack(spacing: 12) {
        ForEach(0..<4, id: \.self) { index in
            Text("Staggered Row \(index + 1)")
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
                .cozyCard(cornerRadius: CozyTheme.Radius.inner)
                .cozyEntrance(delay: CozyTheme.Motion.staggeredDelay(for: index))
        }
    }
    .padding()
    .background(CozyAmbientBackground())
}
