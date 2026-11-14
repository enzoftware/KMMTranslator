//
//  CozyTheme.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI
import UIKit

enum CozyTheme {
    enum Colors {
        static let paperBackground = Color(lightHex: 0xFFFA_F5EC, darkHex: 0xFF17_1412)
        static let paperCard = Color(lightHex: 0xFFFF_FDF8, darkHex: 0xFF22_1E1B)
        static let paperElevated = Color(lightHex: 0xFFFF_FFFF, darkHex: 0xFF2B_2622)
        static let paperMuted = Color(lightHex: 0xFFF1_E9DA, darkHex: 0xFF2F_2A25)

        static let indigo = Color(lightHex: 0xFF4B_4DED, darkHex: 0xFF7D_80F9)
        static let indigoSoft = Color(lightHex: 0xFFEA_E9FD, darkHex: 0xFF2B_2A4A)
        static let onIndigo = Color(lightHex: 0xFFFF_FDF8, darkHex: 0xFF13_111C)

        static let terracotta = Color(lightHex: 0xFFDE_6E52, darkHex: 0xFFF0_8A70)
        static let sage = Color(lightHex: 0xFF3B_8666, darkHex: 0xFF5A_B88E)
        static let danger = Color(lightHex: 0xFFC9_4A38, darkHex: 0xFFEB_6B59)

        static let inkPrimary = Color(lightHex: 0xFF22_1D1A, darkHex: 0xFFF6_EFE6)
        static let inkSecondary = Color(lightHex: 0xFF6B_6058, darkHex: 0xFFB8_ACA1)
        static let inkMuted = Color(lightHex: 0xFF98_8B81, darkHex: 0xFF83_776C)

        static let borderSubtle = Color(lightHex: 0xFFE6_DCCB, darkHex: 0xFF36_302B)
        static let warmShadow = Color(lightHex: 0xFF2A_1F14, darkHex: 0xFF00_0000)

        // Ambient Mesh Palette
        static let meshTopLeading = Color(lightHex: 0xFFFD_F7EB, darkHex: 0xFF19_1513)
        static let meshTopCenter = Color(lightHex: 0xFFF6_EDDF, darkHex: 0xFF1F_1B24)
        static let meshTopTrailing = Color(lightHex: 0xFFEE_E8F7, darkHex: 0xFF1C_1A28)
        static let meshMidCenter = Color(lightHex: 0xFFFA_F3E8, darkHex: 0xFF1E_1917)
        static let meshBottomWarm = Color(lightHex: 0xFFF7_E6D8, darkHex: 0xFF23_1B18)
        static let meshBottomCool = Color(lightHex: 0xFFE9_E7F6, darkHex: 0xFF18_1722)
    }

    enum Radius {
        static let pill: CGFloat = 999
        static let card: CGFloat = 24
        static let inner: CGFloat = 16
        static let badge: CGFloat = 12
    }

    enum Spacing {
        static let xs: CGFloat = 6
        static let sm: CGFloat = 10
        static let md: CGFloat = 16
        static let lg: CGFloat = 22
        static let xl: CGFloat = 30
    }

    enum Motion {
        static let snappySpring = Animation.spring(response: 0.34, dampingFraction: 0.78)
        static let cozySpring = Animation.spring(response: 0.48, dampingFraction: 0.82)
        static let playfulBounce = Animation.spring(response: 0.42, dampingFraction: 0.66)
        static let gentleEase = Animation.easeInOut(duration: 0.28)

        static func staggeredDelay(for index: Int) -> Double {
            Double(min(index, 8)) * 0.045
        }
    }
}

extension Color {
    init(hex: Int64, alpha: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }

    init(lightHex: Int64, darkHex: Int64) {
        self.init(
            uiColor: UIColor(
                light: UIColor(hex: lightHex),
                dark: UIColor(hex: darkHex)
            )
        )
    }
}

extension UIColor {
    convenience init(hex: Int64, alpha: CGFloat = 1.0) {
        self.init(
            red: CGFloat((hex >> 16) & 0xFF) / 255.0,
            green: CGFloat((hex >> 8) & 0xFF) / 255.0,
            blue: CGFloat(hex & 0xFF) / 255.0,
            alpha: alpha
        )
    }

    convenience init(light: UIColor, dark: UIColor) {
        self.init { traits in
            switch traits.userInterfaceStyle {
            case .dark:
                return dark
            default:
                return light
            }
        }
    }
}
