import Foundation

/// Shared storage identity between the main app, the widget extension, and the
/// App Intent (which can run as its own process outside the app's UI).
enum AppGroup {
    static let identifier = "group.com.jasongrech.carlocator"

    static var containerURL: URL {
        FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: identifier)!
    }

    static var storeURL: URL {
        containerURL.appendingPathComponent("CarLocator.sqlite")
    }

    static var defaults: UserDefaults {
        UserDefaults(suiteName: identifier)!
    }
}
