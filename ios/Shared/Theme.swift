import SwiftUI

enum AppColor {
    static let background = Color(hex: 0x16171D)
    static let surface = Color(hex: 0x1E2028)
    static let surfaceRaised = Color(hex: 0x262938)
    static let border = Color(hex: 0x2B2E3B)

    static let textPrimary = Color(hex: 0xF2F3F7)
    static let textSecondary = Color(hex: 0x8B92A3)
    static let textMuted = Color(hex: 0x5C6270)

    static let accent = Color(hex: 0xFF9F1C)
    static let accentPressed = Color(hex: 0xE8890A)
    static let accentTint = Color(hex: 0x3A2A14)
    static let onAccent = Color(hex: 0x1A1400)

    static let success = Color(hex: 0x3BA55D)
    static let danger = Color(hex: 0xED4245)
    static let dangerPressed = Color(hex: 0xC93538)
}

extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8) & 0xFF) / 255
        let b = Double(hex & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}

enum AppFont {
    static func title() -> Font { .system(size: 22, weight: .black) }
    static func eyebrow() -> Font { .system(size: 12, weight: .bold) }
    static func rowTitle() -> Font { .system(size: 16, weight: .semibold) }
    static func body() -> Font { .system(size: 14, weight: .regular) }
    static func caption() -> Font { .system(size: 12, weight: .medium) }
    static func mono(_ size: CGFloat = 14) -> Font { .system(size: size, weight: .semibold, design: .monospaced) }
}
