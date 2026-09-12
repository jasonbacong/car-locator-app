import SwiftUI
import SwiftData
import CoreLocation

struct MainScreen: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.scenePhase) private var scenePhase

    @Query(sort: \ParkingSpot.timestamp, order: .reverse) private var spots: [ParkingSpot]
    @Query private var safeZones: [SafeZone]

    @AppStorage("homeSet", store: AppGroup.defaults) private var homeSet = false
    @AppStorage("homeLat", store: AppGroup.defaults) private var homeLat = 0.0
    @AppStorage("homeLng", store: AppGroup.defaults) private var homeLng = 0.0
    @AppStorage("homeRadius", store: AppGroup.defaults) private var homeRadius = 150.0
    @AppStorage("featureEnabled", store: AppGroup.defaults) private var featureEnabled = true

    @StateObject private var locationService = ObservableLocationService()
    @State private var statusMessage: String?
    @State private var locateTarget: ParkingSpot?
    @State private var authStatus: CLAuthorizationStatus = .notDetermined
    @State private var notificationsAuthorized = false

    var body: some View {
        if let target = locateTarget {
            LocateView(spot: target, onBack: { locateTarget = nil })
        } else {
            ScrollView {
                VStack(alignment: .leading, spacing: 22) {
                    header
                    statusSection
                    parkingDetectionSection
                    homeZoneSection
                    SafeZonesSection(zones: safeZones)
                    testSection
                    historySection
                }
                .padding(20)
            }
            .background(AppColor.background)
            .scrollContentBackground(.hidden)
            .task { await refreshAuthStatus() }
            .onChange(of: scenePhase) { _, newPhase in
                if newPhase == .active {
                    Task { await refreshAuthStatus() }
                }
            }
        }
    }

    private var allReady: Bool {
        authStatus == .authorizedAlways && notificationsAuthorized
    }

    // MARK: - Sections

    private var header: some View {
        HStack {
            ParkingBadge(size: 38)
            Text("CAR LOCATOR")
                .font(AppFont.title())
                .foregroundStyle(AppColor.textPrimary)
            Spacer()
            StatusDot(ready: allReady)
        }
    }

    private var statusSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionEyebrow(text: "Status")
            SectionCard {
                HStack(spacing: 10) {
                    StatusDot(ready: allReady)
                    Text(allReady ? "All set" : "Needs attention")
                        .font(AppFont.rowTitle())
                        .foregroundStyle(AppColor.textPrimary)
                }
                Text(allReady
                     ? "Detection is armed. Set up the Shortcuts automation below to trigger it."
                     : "Location (Always) and notification access are needed for auto-save to work.")
                    .font(AppFont.body())
                    .foregroundStyle(AppColor.textSecondary)

                if authStatus != .authorizedAlways {
                    TactileButton(title: "Grant location access", fullWidth: true) {
                        locationService.requestAlwaysAuthorization()
                    }
                }
                if !notificationsAuthorized {
                    TactileButton(title: "Allow notifications", variant: .secondary, fullWidth: true) {
                        Task {
                            await NotificationService.requestAuthorization()
                            await refreshAuthStatus()
                        }
                    }
                }
            }
        }
    }

    private var parkingDetectionSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionEyebrow(text: "Parking detection")
            SectionCard {
                HStack {
                    Text("Auto-save enabled")
                        .font(AppFont.rowTitle())
                        .foregroundStyle(AppColor.textPrimary)
                    Spacer()
                    Toggle("", isOn: $featureEnabled)
                        .labelsHidden()
                        .tint(AppColor.accent)
                }
                Text("Trigger: a Shortcuts automation, not this app directly. iOS gives no third-party app a way to see a Bluetooth disconnect in the background — Shortcuts is Apple's own bridge for that.")
                    .font(AppFont.body())
                    .foregroundStyle(AppColor.textSecondary)
                Text("Shortcuts → Automation → New Automation → Bluetooth → your car → Disconnects → Run “Save Parking Spot” → turn off “Ask Before Running.”")
                    .font(AppFont.caption())
                    .foregroundStyle(AppColor.textMuted)
            }
        }
    }

    private var homeZoneSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionEyebrow(text: "Home zone")
            SectionCard {
                Text(homeSet ? String(format: "%.5f, %.5f", homeLat, homeLng) : "not set")
                    .font(AppFont.mono())
                    .foregroundStyle(homeSet ? AppColor.textPrimary : AppColor.textMuted)

                TactileButton(title: "Use current location as home", variant: .secondary, fullWidth: true) {
                    Task {
                        locationService.requestWhenInUseAuthorizationIfNeeded()
                        if let location = await locationService.currentLocation() {
                            homeLat = location.coordinate.latitude
                            homeLng = location.coordinate.longitude
                            homeSet = true
                            statusMessage = "Home location updated."
                        } else {
                            statusMessage = "Couldn't get a location fix. Check permissions/GPS."
                        }
                    }
                }

                Text("Skip saving within \(Int(homeRadius)) m of home")
                    .font(AppFont.caption())
                    .foregroundStyle(AppColor.textSecondary)
                Slider(value: $homeRadius, in: 50...500)
                    .tint(AppColor.accent)
            }
        }
    }

    private var testSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionEyebrow(text: "Test")
            SectionCard {
                TactileButton(title: "Save current location now", fullWidth: true) {
                    Task { await runTestSave() }
                }
                if let statusMessage {
                    Text(statusMessage)
                        .font(AppFont.caption())
                        .foregroundStyle(AppColor.textSecondary)
                }
            }
        }
    }

    private var historySection: some View {
        VStack(alignment: .leading, spacing: 10) {
            VStack(alignment: .leading, spacing: 4) {
                SectionEyebrow(text: "History")
                Text("Kept for 60 days, then cleared automatically")
                    .font(AppFont.caption())
                    .foregroundStyle(AppColor.textMuted)
            }

            if spots.isEmpty {
                SectionCard {
                    Text("No spots saved yet.")
                        .font(AppFont.rowTitle())
                        .foregroundStyle(AppColor.textPrimary)
                    Text("Park, disconnect from the car, and this fills in — or use the test button above.")
                        .font(AppFont.body())
                        .foregroundStyle(AppColor.textSecondary)
                }
            } else {
                ForEach(spots) { spot in
                    HistoryRow(
                        spot: spot,
                        onDelete: {
                            modelContext.delete(spot)
                            try? modelContext.save()
                            WidgetReloader.reloadAll()
                        },
                        onLocate: { locateTarget = spot },
                        onSaveNote: { note in
                            spot.note = note
                            try? modelContext.save()
                        },
                        onScheduleReminder: { minutes in
                            Task {
                                await NotificationService.scheduleReminder(minutes: minutes)
                                statusMessage = "Reminder set."
                            }
                        }
                    )
                }
            }
        }
    }

    // MARK: - Actions

    private func refreshAuthStatus() async {
        authStatus = locationService.authorizationStatus
        notificationsAuthorized = await NotificationService.isAuthorized()
    }

    private func runTestSave() async {
        locationService.requestWhenInUseAuthorizationIfNeeded()
        guard let location = await locationService.currentLocation() else {
            statusMessage = "Couldn't get a GPS fix — check location permission and that GPS is on."
            return
        }
        let address = await GeocodingService.reverseGeocode(location)
        let spot = ParkingSpot(
            timestamp: .now,
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude,
            address: address
        )
        modelContext.insert(spot)
        try? modelContext.save()
        WidgetReloader.reloadAll()

        await NotificationService.showSavedNotification(
            lat: spot.latitude,
            lng: spot.longitude,
            timestamp: spot.timestamp.timeIntervalSince1970,
            title: "Parking spot saved",
            body: spot.address ?? String(format: "%.5f, %.5f", spot.latitude, spot.longitude)
        )

        statusMessage = "Saved: \(spot.address ?? String(format: "%.5f, %.5f", spot.latitude, spot.longitude))"
    }
}

/// Thin `ObservableObject` wrapper so the location service's authorization status
/// can drive SwiftUI state without every screen re-implementing CLLocationManagerDelegate.
@MainActor
final class ObservableLocationService: ObservableObject {
    private let service = LocationService()

    var authorizationStatus: CLAuthorizationStatus { service.authorizationStatus }

    func requestWhenInUseAuthorizationIfNeeded() {
        if service.authorizationStatus == .notDetermined {
            service.requestWhenInUseAuthorization()
        }
    }

    func requestAlwaysAuthorization() {
        service.requestAlwaysAuthorization()
    }

    func currentLocation() async -> CLLocation? {
        await service.currentLocation()
    }
}
