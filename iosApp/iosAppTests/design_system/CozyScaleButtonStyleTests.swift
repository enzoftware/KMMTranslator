import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozyScaleButtonStyleTests {

    @Test func buttonStyleDefaultsAndCustomPressedScale() {
        let defaultStyle = CozyScaleButtonStyle()
        let customStyle = CozyScaleButtonStyle(pressedScale: 0.88)

        #expect(defaultStyle.pressedScale == 0.96)
        #expect(customStyle.pressedScale == 0.88)
    }

    @Test func styledButtonRendersNonEmptySnapshot() {
        let view = Button("Tap") {}
            .buttonStyle(CozyScaleButtonStyle(pressedScale: 0.92))

        let renderer = ImageRenderer(content: view)
        renderer.scale = 1
        #expect(renderer.uiImage != nil)
    }
}
