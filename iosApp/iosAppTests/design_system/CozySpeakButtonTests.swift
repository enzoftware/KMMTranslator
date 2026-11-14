import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozySpeakButtonTests {

    @Test func speakButtonStoresStateAndTriggersAction() {
        var tapped = 0
        let button = CozySpeakButton(isSpeaking: true) {
            tapped += 1
        }

        #expect(button.isSpeaking == true)
        button.action()
        #expect(tapped == 1)
    }

    @Test func speakButtonRendersIdleAndSpeakingStates() {
        for speaking in [false, true] {
            let renderer = ImageRenderer(content: CozySpeakButton(isSpeaking: speaking, action: {}))
            renderer.scale = 1
            #expect(renderer.uiImage != nil)
        }
    }
}
