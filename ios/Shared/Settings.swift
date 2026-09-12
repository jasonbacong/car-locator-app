import Foundation

/// Plain read/write over the shared App Group UserDefaults suite — usable outside
/// SwiftUI (the App Intent, the widget). The app's own views read/write the same
/// keys via `@AppStorage(..., store: AppGroup.defaults)` for free reactivity.
enum Settings {
    static var homeSet: Bool { AppGroup.defaults.bool(forKey: "homeSet") }
    static var homeLat: Double { AppGroup.defaults.double(forKey: "homeLat") }
    static var homeLng: Double { AppGroup.defaults.double(forKey: "homeLng") }

    static var homeRadius: Double {
        let value = AppGroup.defaults.double(forKey: "homeRadius")
        return value == 0 ? 150 : value
    }

    static var featureEnabled: Bool {
        AppGroup.defaults.object(forKey: "featureEnabled") == nil
            ? true
            : AppGroup.defaults.bool(forKey: "featureEnabled")
    }

    static func setHome(lat: Double, lng: Double) {
        AppGroup.defaults.set(lat, forKey: "homeLat")
        AppGroup.defaults.set(lng, forKey: "homeLng")
        AppGroup.defaults.set(true, forKey: "homeSet")
    }
}
