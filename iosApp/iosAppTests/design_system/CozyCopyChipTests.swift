import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozyCopyChipTests {

    @Test func triggerCopyInvokesCustomCopyClosureWithText() {
        var copiedText: String?
        let chip = CozyCopyChip(text: "Hola mundo") { text in
            copiedText = text
        }

        chip.triggerCopy()
        #expect(copiedText == "Hola mundo")
    }

    @Test func copyChipRendersNonEmptySnapshot() {
        let chip = CozyCopyChip(text: "Sample")
        let renderer = ImageRenderer(content: chip)
        renderer.scale = 1
        #expect(renderer.uiImage != nil)
    }
}
