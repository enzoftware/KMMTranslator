//
//  Colors.swift
//  iosApp
//
//  Created by Enzo Lizama on 19/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared

extension Color {
    private static let colors = Colors()

    static let lightBlue = CozyTheme.Colors.inkSecondary
    static let lightBlueGrey = CozyTheme.Colors.paperMuted

    static let accentViolet = CozyTheme.Colors.indigo
    static let textBlack = Color(hex: colors.TextBlack)
    static let darkGrey = Color(hex: colors.DarkGrey)

    static let primaryColor = CozyTheme.Colors.indigo
    static let background = CozyTheme.Colors.paperBackground
    static let onPrimary = CozyTheme.Colors.onIndigo
    static let onBackground = CozyTheme.Colors.inkPrimary
    static let surface = CozyTheme.Colors.paperCard
    static let onSurface = CozyTheme.Colors.inkPrimary
}
