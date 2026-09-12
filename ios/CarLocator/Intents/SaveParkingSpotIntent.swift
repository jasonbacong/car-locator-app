import AppIntents
import CoreLocation

/// The actual trigger. Since iOS has no public API for a third-party app to detect
/// a classic-Bluetooth disconnect in the background, this runs instead from a
/// Shortcuts Personal Automation ("When Bluetooth disconnects from <car> → Run
/// this, without asking") — the closest equivalent Apple allows.
struct SaveParkingSpotIntent: AppIntent {
    static var title: LocalizedStringResource = "Save Parking Spot"
    static var description = IntentDescription(
        "Saves your current location as your parking spot, unless you're near home or another safe zone."
    )

    @MainActor
    func perform() async throws -> some IntentResult {
        guard Settings.featureEnabled else {
            return .result()
        }

        let locationService = LocationService()
        guard let location = await locationService.currentLocation() else {
            return .result()
        }

        if isInsideAnySafeZone(location: location) {
            return .result()
        }

        let address = await GeocodingService.reverseGeocode(location)
        let spot = ParkingRepository.save(
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude,
            address: address
        )

        NotificationService.registerCategories()
        await NotificationService.showSavedNotification(
            lat: spot.latitude,
            lng: spot.longitude,
            timestamp: spot.timestamp.timeIntervalSince1970,
            title: "Parking spot saved",
            body: spot.address ?? String(format: "%.5f, %.5f", spot.latitude, spot.longitude)
        )

        WidgetReloader.reloadAll()

        return .result()
    }

    private func isInsideAnySafeZone(location: CLLocation) -> Bool {
        if Settings.homeSet {
            let home = CLLocation(latitude: Settings.homeLat, longitude: Settings.homeLng)
            if location.distance(from: home) < Settings.homeRadius {
                return true
            }
        }
        for zone in ParkingRepository.safeZones() {
            let zoneLocation = CLLocation(latitude: zone.latitude, longitude: zone.longitude)
            if location.distance(from: zoneLocation) < zone.radiusMeters {
                return true
            }
        }
        return false
    }
}

struct CarLocatorShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: SaveParkingSpotIntent(),
            phrases: [
                "Save my parking spot in \(.applicationName)",
                "Save parking spot with \(.applicationName)"
            ],
            shortTitle: "Save Parking Spot",
            systemImageName: "parkingsign.circle.fill"
        )
    }
}
