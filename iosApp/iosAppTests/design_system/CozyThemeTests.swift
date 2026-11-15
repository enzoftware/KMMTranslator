import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozyThemeTests {

    @Test func adaptivePaletteColorsResolveDistinctLightAndDarkValues() {
        let light = UITraitCollection(userInterfaceStyle: .light)
        let dark = UITraitCollection(userInterfaceStyle: .dark)

        let tokens: [Color] = [
            CozyTheme.Colors.paperBackground,
            CozyTheme.Colors.paperCard,
            CozyTheme.Colors.paperElevated,
            CozyTheme.Colors.paperMuted,
            CozyTheme.Colors.indigo,
            CozyTheme.Colors.indigoSoft,
            CozyTheme.Colors.terracotta,
            CozyTheme.Colors.sage,
            CozyTheme.Colors.inkPrimary,
            CozyTheme.Colors.borderSubtle
        ]

        for token in tokens {
            let resolvedLight = UIColor(token).resolvedColor(with: light)
            let resolvedDark = UIColor(token).resolvedColor(with: dark)
            #expect(resolvedLight != resolvedDark)
        }
    }

    @Test func staggeredDelayClampsNegativeAndHighIndices() {
        #expect(CozyTheme.Motion.staggeredDelay(for: -3) == 0.0)
        #expect(CozyTheme.Motion.staggeredDelay(for: 0) == 0.0)
        #expect(abs(CozyTheme.Motion.staggeredDelay(for: 2) - 0.09) < 0.0001)
        #expect(abs(CozyTheme.Motion.staggeredDelay(for: 8) - 0.36) < 0.0001)
        #expect(abs(CozyTheme.Motion.staggeredDelay(for: 42) - 0.36) < 0.0001)
    }

    @Test func spacingAndRadiusTokensIncreaseMonotonically() {
        #expect(CozyTheme.Spacing.xs < CozyTheme.Spacing.sm)
        #expect(CozyTheme.Spacing.sm < CozyTheme.Spacing.md)
        #expect(CozyTheme.Spacing.md < CozyTheme.Spacing.lg)
        #expect(CozyTheme.Spacing.lg < CozyTheme.Spacing.xl)

        #expect(CozyTheme.Radius.badge < CozyTheme.Radius.inner)
        #expect(CozyTheme.Radius.inner < CozyTheme.Radius.card)
        #expect(CozyTheme.Radius.card < CozyTheme.Radius.pill)
    }
}
