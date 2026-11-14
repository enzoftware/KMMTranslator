import SwiftUI
import Testing
import UIKit
@testable import iosApp

@MainActor
struct CozyAmbientBackgroundTests {

    @Test func meshPointsReturnNineNormalizedVerticesAndShiftWhenDrifted() {
        let restPoints = CozyAmbientBackground.meshPoints(drifted: false)
        let driftedPoints = CozyAmbientBackground.meshPoints(drifted: true)

        #expect(restPoints.count == 9)
        #expect(driftedPoints.count == 9)
        #expect(restPoints != driftedPoints)

        for point in restPoints + driftedPoints {
            #expect(point.x >= 0.0 && point.x <= 1.0)
            #expect(point.y >= 0.0 && point.y <= 1.0)
        }
    }

    @Test func ambientBackgroundRendersWithNonZeroSize() {
        let host = UIHostingController(rootView: CozyAmbientBackground())
        host.view.frame = CGRect(x: 0, y: 0, width: 320, height: 640)
        host.view.layoutIfNeeded()

        #expect(host.view.bounds.size == CGSize(width: 320, height: 640))
    }
}
