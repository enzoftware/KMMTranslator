//
//  GradientSurface.swift
//  iosApp
//
//  Created by Enzo Lizama on 22/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI

struct GradientSurface: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(CozyTheme.Colors.paperCard)
    }
}

extension View {
    func gradientSurface() -> some View {
        modifier(GradientSurface())
    }
}
