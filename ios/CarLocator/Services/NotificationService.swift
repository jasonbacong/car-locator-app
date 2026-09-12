import UserNotifications

enum NotificationCategory {
    static let savedSpot = "SAVED_SPOT"
    static let reminder = "REMINDER"
}

enum NotificationAction {
    static let openAppleMaps = "OPEN_APPLE_MAPS"
    static let openWaze = "OPEN_WAZE"
    static let undo = "UNDO_SAVE"
}

enum NotificationService {
    static func registerCategories() {
        let mapsAction = UNNotificationAction(
            identifier: NotificationAction.openAppleMaps, title: "Open in Maps", options: [.foreground]
        )
        let wazeAction = UNNotificationAction(
            identifier: NotificationAction.openWaze, title: "Open in Waze", options: [.foreground]
        )
        let undoAction = UNNotificationAction(
            identifier: NotificationAction.undo, title: "Undo", options: [.destructive]
        )

        let savedCategory = UNNotificationCategory(
            identifier: NotificationCategory.savedSpot,
            actions: [mapsAction, wazeAction, undoAction],
            intentIdentifiers: [],
            options: []
        )
        let reminderCategory = UNNotificationCategory(
            identifier: NotificationCategory.reminder,
            actions: [],
            intentIdentifiers: [],
            options: []
        )
        UNUserNotificationCenter.current().setNotificationCategories([savedCategory, reminderCategory])
    }

    static func requestAuthorization() async {
        _ = try? await UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge])
    }

    static func isAuthorized() async -> Bool {
        let settings = await UNUserNotificationCenter.current().notificationSettings()
        return settings.authorizationStatus == .authorized
    }

    static func showSavedNotification(lat: Double, lng: Double, timestamp: TimeInterval, title: String, body: String) async {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.categoryIdentifier = NotificationCategory.savedSpot
        content.userInfo = ["lat": lat, "lng": lng, "timestamp": timestamp]
        content.sound = .default

        let request = UNNotificationRequest(identifier: "saved-\(timestamp)", content: content, trigger: nil)
        try? await UNUserNotificationCenter.current().add(request)
    }

    static func scheduleReminder(minutes: Double) async {
        let content = UNMutableNotificationContent()
        content.title = "Parking reminder"
        content.body = "Check your meter or time limit — you set this reminder from Car Locator."
        content.categoryIdentifier = NotificationCategory.reminder
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: minutes * 60, repeats: false)
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)
        try? await UNUserNotificationCenter.current().add(request)
    }
}
