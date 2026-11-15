//
//  CozyCopyChip.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI
import UIKit

struct CozyCopyChip: View {
    let text: String
    var onCopy: ((String) -> Void)? = nil

    @State private var copied = false
    @State private var resetTask: Task<Void, Never>?

    var body: some View {
        Button {
            triggerCopy()
        } label: {
            HStack(spacing: 5) {
                Image(systemName: copied ? "checkmark" : "doc.on.doc")
                    .font(.caption.weight(.semibold))
                    .contentTransition(.symbolEffect(.replace))
                Text(copied ? "Copied" : "Copy")
                    .font(.caption.weight(.semibold))
            }
            .foregroundStyle(copied ? CozyTheme.Colors.sage : CozyTheme.Colors.inkSecondary)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(
                Capsule()
                    .fill(
                        copied
                            ? CozyTheme.Colors.sage.opacity(0.14)
                            : CozyTheme.Colors.paperMuted.opacity(0.85)
                    )
            )
            .animation(CozyTheme.Motion.snappySpring, value: copied)
        }
        .buttonStyle(CozyScaleButtonStyle(pressedScale: 0.94))
        .sensoryFeedback(.success, trigger: copied)
        .accessibilityLabel(copied ? "Copied to clipboard" : "Copy text")
        .onDisappear {
            resetTask?.cancel()
        }
    }

    func triggerCopy() {
        if let onCopy {
            onCopy(text)
        } else {
            UIPasteboard.general.string = text
        }
        withAnimation(CozyTheme.Motion.snappySpring) {
            copied = true
        }
        resetTask?.cancel()
        resetTask = Task { @MainActor in
            try? await Task.sleep(for: .seconds(1.5))
            guard !Task.isCancelled else { return }
            withAnimation(CozyTheme.Motion.gentleEase) {
                copied = false
            }
        }
    }
}

#Preview("CozyCopyChip") {
    HStack(spacing: 16) {
        CozyCopyChip(text: "Hello world")
    }
    .padding(32)
    .background(CozyAmbientBackground())
}
