//
//  PhrasebookScreen.swift
//  iosApp
//

import SwiftUI
import shared

enum PhrasebookTab: Hashable {
    case suggested
    case myWords
}

extension PhrasebookCategory {
    static let ordered: [PhrasebookCategory] = [.greetings, .courtesy, .conversation, .travel, .food]

    var title: String {
        switch self {
        case .greetings: return "Greetings"
        case .courtesy: return "Courtesy"
        case .conversation: return "Conversation"
        case .travel: return "Travel"
        case .food: return "Food"
        default: return ""
        }
    }
}

/// Offline phrasebook: built-in suggestions for the current pair that can be saved to "My Words"
/// (and removed again), plus the saved list across all pairs. Mirrors the Android PhrasebookScreen.
struct PhrasebookScreen: View {
    let fromLanguage: UiLanguage
    let toLanguage: UiLanguage
    /// Opens a phrase in the translator; language codes are set for saved words (their own pair).
    let onUsePhrase: (_ entryKey: String, _ fromCode: String?, _ toCode: String?) -> Void

    @StateObject private var viewModel: IOSPhrasebookViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var tab: PhrasebookTab

    init(
        savedWordsDataSource: SavedWordsDataSource,
        fromLanguage: UiLanguage,
        toLanguage: UiLanguage,
        initialTab: PhrasebookTab = .suggested,
        onUsePhrase: @escaping (_ entryKey: String, _ fromCode: String?, _ toCode: String?) -> Void
    ) {
        self._tab = State(initialValue: initialTab)
        self.fromLanguage = fromLanguage
        self.toLanguage = toLanguage
        self.onUsePhrase = onUsePhrase
        self._viewModel = StateObject(
            wrappedValue: IOSPhrasebookViewModel(savedWordsDataSource: savedWordsDataSource)
        )
    }

    var body: some View {
        ZStack {
            CozyAmbientBackground()

            VStack(alignment: .leading, spacing: CozyTheme.Spacing.md) {
                topBar
                    .cozyEntrance(delay: 0.0)
                tabSwitch
                    .cozyEntrance(delay: 0.05)

                switch tab {
                case .suggested:
                    suggestedList
                case .myWords:
                    myWordsList
                }
            }
            .padding(.horizontal, CozyTheme.Spacing.md)
            .padding(.top, CozyTheme.Spacing.sm)
        }
        .toolbar(.hidden, for: .navigationBar)
        .animation(CozyTheme.Motion.cozySpring, value: tab)
        .onAppear {
            viewModel.startObserving(fromLanguage: fromLanguage, toLanguage: toLanguage)
        }
        .onDisappear {
            viewModel.dispose()
        }
    }

    // MARK: - Header

    private var topBar: some View {
        HStack(spacing: 12) {
            CozyGlassContainer(spacing: 10) {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.subheadline.weight(.bold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .frame(width: 40, height: 40)
                        .cozyFloatingChrome(in: Circle(), interactive: true)
                }
                .buttonStyle(CozyScaleButtonStyle())
                .accessibilityLabel("Back to translator")
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("Phrasebook")
                    .font(.system(.title2, design: .rounded, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
                Text("Offline words for \(fromLanguage.language.languageName) → \(toLanguage.language.languageName)")
                    .font(.caption)
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)
            }

            Spacer()
        }
    }

    private var tabSwitch: some View {
        let savedCount = viewModel.state.savedWords.count
        return HStack(spacing: 4) {
            tabButton(title: "Suggested", tab: .suggested)
            tabButton(title: savedCount > 0 ? "My Words · \(savedCount)" : "My Words", tab: .myWords)
        }
        .padding(4)
        .cozyFloatingChrome(in: Capsule(), interactive: true)
    }

    private func tabButton(title: String, tab target: PhrasebookTab) -> some View {
        let isSelected = tab == target
        return Button {
            tab = target
        } label: {
            Text(title)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(isSelected ? CozyTheme.Colors.onIndigo : CozyTheme.Colors.inkSecondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(Capsule().fill(isSelected ? CozyTheme.Colors.indigo : Color.clear))
                .contentShape(Capsule())
        }
        .buttonStyle(CozyScaleButtonStyle())
        .accessibilityAddTraits(isSelected ? .isSelected : [])
        .sensoryFeedback(.selection, trigger: tab)
    }

    // MARK: - Suggested

    private var suggestedList: some View {
        VStack(alignment: .leading, spacing: CozyTheme.Spacing.sm) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    categoryChip(title: "All", category: nil)
                    ForEach(PhrasebookCategory.ordered, id: \.self) { category in
                        categoryChip(title: category.title, category: category)
                    }
                }
            }

            ScrollView {
                LazyVStack(spacing: CozyTheme.Spacing.sm) {
                    ForEach(viewModel.state.suggestions, id: \.entryKey) { phrase in
                        phraseCard(phrase)
                    }
                }
                .padding(.bottom, CozyTheme.Spacing.xl)
            }
            .scrollIndicators(.hidden)
        }
        .transition(.opacity)
    }

    private func categoryChip(title: String, category: PhrasebookCategory?) -> some View {
        let isSelected = viewModel.state.selectedCategory == category
        return Button {
            withAnimation(CozyTheme.Motion.snappySpring) {
                viewModel.onEvent(PhrasebookEvent.SelectCategory(category: category))
            }
        } label: {
            Text(title)
                .font(.caption.weight(.semibold))
                .foregroundStyle(isSelected ? CozyTheme.Colors.indigo : CozyTheme.Colors.inkSecondary)
                .padding(.horizontal, 12)
                .padding(.vertical, 7)
                .background(
                    Capsule().fill(isSelected ? CozyTheme.Colors.indigoSoft : CozyTheme.Colors.paperMuted.opacity(0.65))
                )
                .overlay(
                    Capsule().strokeBorder(
                        isSelected ? CozyTheme.Colors.indigo.opacity(0.4) : CozyTheme.Colors.borderSubtle,
                        lineWidth: 1
                    )
                )
        }
        .buttonStyle(CozyScaleButtonStyle())
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }

    private func phraseCard(_ phrase: UiPhrase) -> some View {
        HStack(spacing: 8) {
            Button {
                onUsePhrase(phrase.entryKey, nil, nil)
                dismiss()
            } label: {
                VStack(alignment: .leading, spacing: 3) {
                    Text(phrase.toText)
                        .font(.system(.headline, design: .serif, weight: .semibold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    Text(phrase.fromText)
                        .font(.caption)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityHint("Opens in the translator")

            Button {
                withAnimation(CozyTheme.Motion.snappySpring) {
                    viewModel.onEvent(PhrasebookEvent.ToggleSaved(entryKey: phrase.entryKey))
                }
            } label: {
                Image(systemName: phrase.isSaved ? "bookmark.fill" : "bookmark")
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(phrase.isSaved ? CozyTheme.Colors.indigo : CozyTheme.Colors.inkSecondary)
                    .contentTransition(.symbolEffect(.replace))
                    .frame(width: 40, height: 40)
                    .background(
                        Circle().fill(phrase.isSaved ? CozyTheme.Colors.indigoSoft : CozyTheme.Colors.paperMuted.opacity(0.6))
                    )
            }
            .buttonStyle(CozyScaleButtonStyle(pressedScale: 0.9))
            .sensoryFeedback(.selection, trigger: phrase.isSaved)
            .accessibilityLabel(phrase.isSaved ? "Remove \(phrase.fromText) from My Words" : "Save \(phrase.fromText) to My Words")
        }
        .padding(.leading, CozyTheme.Spacing.md)
        .padding(.trailing, 8)
        .padding(.vertical, 12)
        .cozyCard(cornerRadius: 20)
    }

    // MARK: - My Words

    @ViewBuilder
    private var myWordsList: some View {
        if viewModel.state.savedWords.isEmpty {
            HStack(spacing: 14) {
                ZStack {
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .fill(CozyTheme.Colors.indigoSoft)
                        .frame(width: 46, height: 46)
                    Image(systemName: "bookmark.square.fill")
                        .font(.title3)
                        .foregroundStyle(CozyTheme.Colors.indigo)
                }
                VStack(alignment: .leading, spacing: 3) {
                    Text("No saved words yet")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    Text("Tap the bookmark on any suggestion to keep it here. Swipe a word away to remove it.")
                        .font(.caption)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(CozyTheme.Spacing.md)
            .cozyCard(cornerRadius: 22)
            .transition(.opacity)
            Spacer()
        } else {
            // A List gives native swipe-to-delete; it is styled to look like the cozy cards.
            List {
                ForEach(viewModel.state.savedWords, id: \.self) { word in
                    savedWordCard(word)
                        .listRowBackground(Color.clear)
                        .listRowSeparator(.hidden)
                        .listRowInsets(EdgeInsets(top: 5, leading: 0, bottom: 5, trailing: 0))
                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                            Button(role: .destructive) {
                                remove(word)
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                }
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
            .transition(.opacity)
        }
    }

    private func savedWordCard(_ word: UiSavedWord) -> some View {
        HStack(spacing: 8) {
            Button {
                onUsePhrase(word.entryKey, word.fromLanguage.language.code, word.toLanguage.language.code)
                dismiss()
            } label: {
                VStack(alignment: .leading, spacing: 6) {
                    HStack(spacing: 5) {
                        SmallLanguageIcon(language: word.fromLanguage, size: 16)
                        Image(systemName: "arrow.right")
                            .font(.system(size: 8, weight: .bold))
                            .foregroundStyle(CozyTheme.Colors.inkMuted)
                        SmallLanguageIcon(language: word.toLanguage, size: 16)
                    }
                    Text(word.toText)
                        .font(.system(.headline, design: .serif, weight: .semibold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    Text(word.fromText)
                        .font(.caption)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityHint("Opens in the translator")

            Button {
                remove(word)
            } label: {
                Image(systemName: "xmark")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)
                    .frame(width: 30, height: 30)
                    .background(Circle().fill(CozyTheme.Colors.paperMuted.opacity(0.85)))
            }
            .buttonStyle(CozyScaleButtonStyle())
            .accessibilityLabel("Delete \(word.fromText)")
        }
        .padding(.leading, CozyTheme.Spacing.md)
        .padding(.trailing, 8)
        .padding(.vertical, 12)
        .cozyCard(cornerRadius: 20)
    }

    private func remove(_ word: UiSavedWord) {
        withAnimation(CozyTheme.Motion.snappySpring) {
            viewModel.onEvent(
                PhrasebookEvent.RemoveSavedWord(
                    entryKey: word.entryKey,
                    fromLanguageCode: word.fromLanguage.language.code,
                    toLanguageCode: word.toLanguage.language.code
                )
            )
        }
    }
}
