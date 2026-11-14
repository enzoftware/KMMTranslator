import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozyCardTests {

    @Test func modifierPreservesConfiguration() {
        let modifier = CozyCardModifier(cornerRadius: 18, isElevated: true)
        #expect(modifier.cornerRadius == 18)
        #expect(modifier.isElevated == true)
    }

    @Test func cozyCardRendersNonEmptySnapshot() {
        let view = Text("Cozy Card Content")
            .padding()
            .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)

        let renderer = ImageRenderer(content: view)
        renderer.scale = 1
        #expect(renderer.uiImage != nil)
    }
}
