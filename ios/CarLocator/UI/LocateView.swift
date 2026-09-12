import SwiftUI
import CoreLocation

/// Distance + a rotating arrow pointing at the saved spot — for parking garages
/// where Maps/Waze themselves lose GPS accuracy between concrete levels.
struct LocateView: View {
    let spot: ParkingSpot
    let onBack: () -> Void

    @StateObject private var tracker = LiveTracker()

    private var target: CLLocation {
        CLLocation(latitude: spot.latitude, longitude: spot.longitude)
    }

    private var distanceMeters: Double? {
        tracker.location.map { $0.distance(from: target) }
    }

    private var arrowRotation: Double {
        guard let current = tracker.location else { return 0 }
        return current.bearing(to: target) - tracker.headingDegrees
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                TactileButton(title: "Back", variant: .secondary, action: onBack)
                Spacer()
            }

            Spacer().frame(height: 32)

            Text(spot.address ?? String(format: "%.5f, %.5f", spot.latitude, spot.longitude))
                .font(AppFont.rowTitle())
                .foregroundStyle(AppColor.textPrimary)

            Spacer().frame(height: 24)

            Image(systemName: "location.north.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 72, height: 72)
                .foregroundStyle(AppColor.accent)
                .rotationEffect(.degrees(arrowRotation))
                .frame(width: 160, height: 160)

            Spacer().frame(height: 24)

            if let distanceMeters {
                Text(distanceMeters >= 1000
                     ? String(format: "%.1f km", distanceMeters / 1000)
                     : String(format: "%.0f m", distanceMeters))
                    .font(AppFont.mono(28))
                    .foregroundStyle(AppColor.textPrimary)
                Text("away, in the direction of the arrow")
                    .font(AppFont.body())
                    .foregroundStyle(AppColor.textSecondary)
            } else {
                Text("Getting your location…")
                    .font(AppFont.body())
                    .foregroundStyle(AppColor.textMuted)
            }

            Spacer().frame(height: 32)

            TactileButton(title: "Open in Maps", fullWidth: true) {
                UIApplication.shared.open(NavLinks.appleMapsURL(lat: spot.latitude, lng: spot.longitude))
            }
            Spacer().frame(height: 10)
            TactileButton(title: "Open in Waze", variant: .secondary, fullWidth: true) {
                let url = NavLinks.wazeURL(lat: spot.latitude, lng: spot.longitude)
                if UIApplication.shared.canOpenURL(url) {
                    UIApplication.shared.open(url)
                } else {
                    UIApplication.shared.open(NavLinks.wazeWebURL(lat: spot.latitude, lng: spot.longitude))
                }
            }

            Spacer()
        }
        .padding(20)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppColor.background)
        .onAppear { tracker.start() }
        .onDisappear { tracker.stop() }
    }
}
