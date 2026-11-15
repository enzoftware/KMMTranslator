import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozyGlassContainerTests {

    @Test func containerUsesDefaultAndCustomSpacing() {
        let defaultContainer = CozyGlassContainer {
            Text("Default")
        }
        let customContainer = CozyGlassContainer(spacing: 24) {
            Text("Custom")
        }

        #expect(defaultContainer.spacing == 12)
        #expect(customContainer.spacing == 24)
    }

    @Test func containerRendersChildContent() {
        let container = CozyGlassContainer(spacing: 10) {
            HStack {
                Text("One")
                Text("Two")
            }
        }

        let host = UIHostingController(rootView: container)
        host.view.frame = CGRect(x: 0, y: 0, width: 200, height: 60)
        host.view.layoutIfNeeded()
        #expect(host.view.bounds.size == CGSize(width: 200, height: 60))
    }
}
