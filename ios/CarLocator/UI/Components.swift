import SwiftUI

enum ButtonVariant {
    case primary, secondary, danger
}

/// A pill button whose press feedback is the "tactile" feel: scale down + darken
/// on touch-down, spring back on release. Mirrors the Android version's approach.
struct TactileButton: View {
    let title: String
    var variant: ButtonVariant = .primary
    var fullWidth: Bool = false
    var enabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(AppFont.body().bold())
                .foregroundStyle(contentColor)
                .padding(.horizontal, 20)
                .padding(.vertical, 14)
                .frame(maxWidth: fullWidth ? .infinity : nil)
                .background(
                    Capsule()
                        .fill(backgroundColor)
                        .overlay(borderOverlay)
                )
        }
        .buttonStyle(TactileButtonStyle())
        .disabled(!enabled)
        .opacity(enabled ? 1 : 0.5)
    }

    @ViewBuilder
    private var borderOverlay: some View {
        if variant == .secondary {
            Capsule().stroke(AppColor.border, lineWidth: 1)
        }
    }

    private var backgroundColor: Color {
        switch variant {
        case .primary: return AppColor.accent
        case .secondary: return AppColor.surface
        case .danger: return AppColor.danger
        }
    }

    private var contentColor: Color {
        switch variant {
        case .primary: return AppColor.onAccent
        case .secondary: return AppColor.textPrimary
        case .danger: return AppColor.textPrimary
        }
    }
}

struct TactileButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.96 : 1)
            .brightness(configuration.isPressed ? -0.06 : 0)
            .animation(.spring(response: 0.25, dampingFraction: 0.6), value: configuration.isPressed)
    }
}

/// A flat rounded block — the basic layout unit, no shadow, stepped one tone above the page.
struct SectionCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            content
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RoundedRectangle(cornerRadius: 20).fill(AppColor.surface))
    }
}

struct SectionEyebrow: View {
    let text: String

    var body: some View {
        Text(text.uppercased())
            .font(AppFont.eyebrow())
            .tracking(1.5)
            .foregroundStyle(AppColor.textSecondary)
    }
}

struct StatusDot: View {
    let ready: Bool

    var body: some View {
        Circle()
            .fill(ready ? AppColor.success : AppColor.accent)
            .frame(width: 8, height: 8)
    }
}

/// The app's signature mark: a blocky rounded-square parking badge.
struct ParkingBadge: View {
    var size: CGFloat = 40

    var body: some View {
        RoundedRectangle(cornerRadius: size * 0.3)
            .fill(AppColor.accentTint)
            .frame(width: size, height: size)
            .overlay(
                Text("P")
                    .font(.system(size: size * 0.46, weight: .black))
                    .foregroundStyle(AppColor.accent)
            )
    }
}
