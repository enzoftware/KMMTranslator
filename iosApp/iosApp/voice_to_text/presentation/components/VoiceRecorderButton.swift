//
//  VoiceRecorderButton.swift
//  iosApp
//
//  Created by Enzo Lizama on 24/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct VoiceRecorderButton: View {

    var displayState: DisplayState
    var onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            ZStack {

                Circle()
                    .foregroundColor(.primaryColor)
                    .padding()

                icon
                    .foregroundColor(.onPrimary)
            }
        }
        .frame(maxWidth: 100, maxHeight: 100)
    }

    @ViewBuilder
    private var icon: some View {
        switch displayState {
        case .speaking:
            Image(systemName: "stop.fill")
        case .displayingResults:
            Image(systemName: "checkmark")
        default:
            Image(uiImage: UIImage(named: "mic")!)

        }
    }
}

#Preview {
    VoiceRecorderButton(
        displayState: .displayingResults,
        onClick: {

        }
    )
}
