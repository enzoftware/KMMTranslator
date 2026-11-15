//
//  CozyGlassContainer.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

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

#Preview("CozyGlassContainer") {
    CozyGlassContainer(spacing: 12) {
        HStack(spacing: 12) {
            Text("First")
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .cozyFloatingChrome(in: Capsule())

            Text("Second")
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .cozyFloatingChrome(in: Capsule())
        }
    }
    .padding(32)
    .background(CozyAmbientBackground())
}
