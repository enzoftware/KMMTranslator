import Foundation
import SwiftUI
import Testing
import UIKit
import shared
@testable import iosApp

struct ColorsTests {

    @Test func colorHexInitializerExtractsRGBAndAlpha() {
        let color = UIColor(Color(hex: 0x336699, alpha: 0.5))
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0

        let converted = color.getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        #expect(converted)
        #expect(abs(red - (0x33 / 255.0)) < 0.001)
        #expect(abs(green - (0x66 / 255.0)) < 0.001)
        #expect(abs(blue - (0x99 / 255.0)) < 0.001)
        #expect(abs(alpha - 0.5) < 0.001)
    }

    @Test func adaptiveColorsResolveForLightAndDarkTraits() {
        let lightTraits = UITraitCollection(userInterfaceStyle: .light)
        let darkTraits = UITraitCollection(userInterfaceStyle: .dark)

        let bgLight = UIColor(Color.background).resolvedColor(with: lightTraits)
        let bgDark = UIColor(Color.background).resolvedColor(with: darkTraits)
        #expect(bgLight != bgDark)

        let onSurfaceLight = UIColor(Color.onSurface).resolvedColor(with: lightTraits)
        let onSurfaceDark = UIColor(Color.onSurface).resolvedColor(with: darkTraits)
        #expect(onSurfaceLight != onSurfaceDark)
    }
}

struct MicrophonePowerObserverTests {

    @Test func normalizedPowerRatioClampsBelowMinus50AndAboveZero() {
        #expect(MicrophonePowerObserver.normalizedPowerRatio(for: -80.0) == 0.0)
        #expect(MicrophonePowerObserver.normalizedPowerRatio(for: -50.0) == 0.0)
        #expect(abs(MicrophonePowerObserver.normalizedPowerRatio(for: -25.0) - 0.5) < 0.0001)
        #expect(MicrophonePowerObserver.normalizedPowerRatio(for: 0.0) == 1.0)
        #expect(MicrophonePowerObserver.normalizedPowerRatio(for: 12.0) == 1.0)
    }

    @Test func releaseRemovesTemporaryRecordingFileAndResetsPowerRatio() throws {
        let observer = MicrophonePowerObserver()
        try Data("dummy".utf8).write(to: observer.tempFile)
        #expect(FileManager.default.fileExists(atPath: observer.tempFile.path))

        observer.release()

        #expect(observer.micPowerRatio == 0.0)
        #expect(!FileManager.default.fileExists(atPath: observer.tempFile.path))
    }
}
