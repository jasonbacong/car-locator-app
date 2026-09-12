import UIKit
import UserNotifications

/// Handles taps on the saved-spot notification's actions (Maps / Waze / Undo).
final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let info = response.notification.request.content.userInfo
        let lat = info["lat"] as? Double
        let lng = info["lng"] as? Double
        let timestamp = info["timestamp"] as? Double
        let actionIdentifier = response.actionIdentifier

        Task { @MainActor in
            switch actionIdentifier {
            case NotificationAction.openAppleMaps:
                if let lat, let lng {
                    UIApplication.shared.open(NavLinks.appleMapsURL(lat: lat, lng: lng))
                }
            case NotificationAction.openWaze:
                if let lat, let lng {
                    let url = NavLinks.wazeURL(lat: lat, lng: lng)
                    if UIApplication.shared.canOpenURL(url) {
                        UIApplication.shared.open(url)
                    } else {
                        UIApplication.shared.open(NavLinks.wazeWebURL(lat: lat, lng: lng))
                    }
                }
            case NotificationAction.undo:
                if let timestamp {
                    ParkingRepository.deleteSpot(withTimestamp: timestamp)
                    WidgetReloader.reloadAll()
                }
            default:
                break
            }
            completionHandler()
        }
    }
}
