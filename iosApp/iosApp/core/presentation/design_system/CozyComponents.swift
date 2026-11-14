//
//  CozyComponents.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI
import UIKit
import UniformTypeIdentifiers

struct CozyScaleButtonStyle: ButtonStyle {
    var pressedScale: CGFloat = 0.96

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? pressedScale : 1.0)
            .opacity(configuration.isPressed ? 0.9 : 1.0)
            .animation(CozyTheme.Motion.snappySpring, value: configuration.isPressed)
    }
}

struct CozyCopyChip: View {
    let text: String
    var label: String? = nil

    @State private var didCopy = false
    @State private var resetTask: Task<Void, Never>?

    var body: some View {
        Button {
            UIPasteboard.general.setValue(
                text,
                forPasteboardType: UTType.plainText.identifier
            )
            withAnimation(CozyTheme.Motion.snappySpring) {
                didCopy = true
            }
            resetTask?.cancel()
            resetTask = Task {
                try? await Task.sleep(for: .seconds(1.6))
                guard !Task.isCancelled else { return }
                await MainActor.run {
                    withAnimation(CozyTheme.Motion.gentleEase) {
                        didCopy = false
                    }
                }
            }
        } label: {
            HStack(spacing: 5) {
                Image(systemName: didCopy ? "checkmark" : "doc.on.doc")
                    .font(.system(size: 13, weight: .semibold))
                    .contentTransition(.symbolEffect(.replace))
                if didCopy {
                    Text("Copied")
                        .font(.caption.weight(.semibold))
                        .transition(.blurReplace)
                } else if let label {
                    Text(label)
                        .font(.caption.weight(.medium))
                        .transition(.blurReplace)
                }
            }
            .foregroundStyle(didCopy ? CozyTheme.Colors.sage : CozyTheme.Colors.inkSecondary)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(
                Capsule()
                    .fill(
                        didCopy
                            ? CozyTheme.Colors.sage.opacity(0.14)
                            : CozyTheme.Colors.paperMuted.opacity(0.75)
                    )
            )
        }
        .buttonStyle(CozyScaleButtonStyle())
        .sensoryFeedback(.success, trigger: didCopy) { _, newValue in
            newValue
        }
        .accessibilityLabel(didCopy ? "Copied" : "Copy text")
    }
}

struct CozySpeakButton: View {
    let isSpeaking: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 5) {
                Image(systemName: "speaker.wave.2.fill")
                    .font(.system(size: 13, weight: .semibold))
                    .symbolEffect(.variableColor.iterative, isActive: isSpeaking)
                Text(isSpeaking ? "Speaking" : "Listen")
                    .font(.caption.weight(.medium))
            }
            .foregroundStyle(isSpeaking ? CozyTheme.Colors.indigo : CozyTheme.Colors.inkSecondary)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(
                Capsule()
                    .fill(
                        isSpeaking
                            ? CozyTheme.Colors.indigoSoft
                            : CozyTheme.Colors.paperMuted.opacity(0.75)
                    )
            )
        }
        .buttonStyle(CozyScaleButtonStyle())
        .sensoryFeedback(.selection, trigger: isSpeaking)
        .accessibilityLabel("Speak translation")
    }
}

struct CozyBannerToast: View {
    let message: String
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(CozyTheme.Colors.danger)
                .symbolEffect(.pulse)

            Text(message)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(CozyTheme.Colors.inkPrimary)
                .multilineTextAlignment(.leading)

            Spacer(minLength: 4)

            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)
                    .padding(6)
                    .background(Circle().fill(CozyTheme.Colors.paperMuted))
            }
            .buttonStyle(CozyScaleButtonStyle())
            .accessibilityLabel("Dismiss error")
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous)
                .fill(CozyTheme.Colors.paperElevated)
                .overlay(
                    RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous)
                        .strokeBorder(CozyTheme.Colors.danger.opacity(0.35), lineWidth: 1)
                )
        )
        .shadow(
            color: CozyTheme.Colors.warmShadow.opacity(0.14),
            radius: 12,
            x: 0,
            y: 6
        )
    }
}
