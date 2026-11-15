//
//  CozyBannerToast.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

struct CozyBannerToast: View {
    let message: String
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(CozyTheme.Colors.danger)

            Text(message)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(CozyTheme.Colors.inkPrimary)
                .multilineTextAlignment(.leading)

            Spacer(minLength: 8)

            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)
                    .frame(width: 26, height: 26)
                    .background(Circle().fill(CozyTheme.Colors.paperMuted))
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Dismiss error")
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .cozyFloatingChrome(
            in: RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous),
            tint: CozyTheme.Colors.paperElevated,
            interactive: true
        )
    }
}

#Preview("CozyBannerToast") {
    CozyBannerToast(
        message: "Couldn't reach the translation service. Please try again.",
        onDismiss: {}
    )
    .padding()
    .background(CozyAmbientBackground())
}
