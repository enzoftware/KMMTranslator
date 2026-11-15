import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozyFloatingChromeTests {

    @Test func modifierStoresShapeTintAndInteractivity() {
        let modifier = CozyFloatingChromeModifier(
            shape: Capsule(),
            tint: CozyTheme.Colors.indigo,
            interactive: false
        )
        #expect(modifier.tint == CozyTheme.Colors.indigo)
        #expect(modifier.interactive == false)
    }

    @Test func floatingChromeHostsNeutralAndTintedViews() {
        let view = VStack(spacing: 12) {
            Text("Neutral")
                .padding()
                .cozyFloatingChrome(in: Capsule())
            Text("Tinted")
                .padding()
                .cozyFloatingChrome(in: Circle(), tint: CozyTheme.Colors.indigo, interactive: true)
        }

        let host = UIHostingController(rootView: view)
        host.view.frame = CGRect(x: 0, y: 0, width: 240, height: 180)
        host.view.layoutIfNeeded()
        #expect(host.view.bounds.size == CGSize(width: 240, height: 180))
    }
}
