import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozyEntranceTests {

    @Test func modifierPreservesDelayAndOffset() {
        let modifier = CozyEntranceModifier(delay: 0.12, offsetY: 20)
        #expect(modifier.delay == 0.12)
        #expect(modifier.offsetY == 20)
    }

    @Test func cozyEntranceRendersContent() {
        let view = Text("Appearing Soon")
            .padding()
            .cozyEntrance(delay: 0.05, offsetY: 14)

        let host = UIHostingController(rootView: view)
        host.view.frame = CGRect(x: 0, y: 0, width: 220, height: 80)
        host.view.layoutIfNeeded()
        #expect(host.view.bounds.size == CGSize(width: 220, height: 80))
    }
}
