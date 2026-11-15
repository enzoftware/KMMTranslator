import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozyBannerToastTests {

    @Test func bannerToastStoresMessageAndInvokesOnDismiss() {
        var dismissed = false
        let toast = CozyBannerToast(message: "Network timeout") {
            dismissed = true
        }

        #expect(toast.message == "Network timeout")
        toast.onDismiss()
        #expect(dismissed == true)
    }

    @Test func bannerToastRendersCleanly() {
        let host = UIHostingController(
            rootView: CozyBannerToast(message: "Unable to translate", onDismiss: {})
        )
        host.view.frame = CGRect(x: 0, y: 0, width: 320, height: 80)
        host.view.layoutIfNeeded()
        #expect(host.view.bounds.size == CGSize(width: 320, height: 80))
    }
}
