import SwiftUI
import SwiftData
import UserNotifications

@main
struct CarLocatorApp: App {
    private let notificationDelegate = NotificationDelegate()

    init() {
        UNUserNotificationCenter.current().delegate = notificationDelegate
        NotificationService.registerCategories()
    }

    var body: some Scene {
        WindowGroup {
            MainScreen()
                .preferredColorScheme(.dark)
        }
        .modelContainer(PersistenceController.shared)
    }
}
