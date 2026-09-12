import WidgetKit
import SwiftUI
import SwiftData

struct ParkingEntry: TimelineEntry {
    let date: Date
    let spot: ParkingSpot?
}

struct ParkingProvider: TimelineProvider {
    func placeholder(in context: Context) -> ParkingEntry {
        ParkingEntry(date: .now, spot: nil)
    }

    func getSnapshot(in context: Context, completion: @escaping (ParkingEntry) -> Void) {
        completion(ParkingEntry(date: .now, spot: fetchLatestSpot()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<ParkingEntry>) -> Void) {
        let entry = ParkingEntry(date: .now, spot: fetchLatestSpot())
        completion(Timeline(entries: [entry], policy: .never))
    }

    private func fetchLatestSpot() -> ParkingSpot? {
        let modelContext = ModelContext(PersistenceController.shared)
        var descriptor = FetchDescriptor<ParkingSpot>(sortBy: [SortDescriptor(\.timestamp, order: .reverse)])
        descriptor.fetchLimit = 1
        return try? modelContext.fetch(descriptor).first
    }
}

struct ParkingWidgetView: View {
    let entry: ParkingEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("CAR LOCATOR")
                .font(.system(size: 10, weight: .bold))
                .tracking(1.2)
                .foregroundStyle(AppColor.textSecondary)

            if let spot = entry.spot {
                Text(spot.address ?? String(format: "%.5f, %.5f", spot.latitude, spot.longitude))
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(AppColor.textPrimary)
                    .lineLimit(1)
                Text(spot.timestamp, style: .relative)
                    .font(.system(size: 11))
                    .foregroundStyle(AppColor.textSecondary)

                Spacer(minLength: 4)

                HStack(spacing: 8) {
                    Link(destination: NavLinks.appleMapsURL(lat: spot.latitude, lng: spot.longitude)) {
                        widgetPill("Maps")
                    }
                    Link(destination: NavLinks.wazeURL(lat: spot.latitude, lng: spot.longitude)) {
                        widgetPill("Waze")
                    }
                }
            } else {
                Text("No spot saved")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(AppColor.textPrimary)
                Text("Park and disconnect to save one")
                    .font(.system(size: 11))
                    .foregroundStyle(AppColor.textSecondary)
                Spacer()
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        .containerBackground(AppColor.background, for: .widget)
    }

    private func widgetPill(_ title: String) -> some View {
        Text(title)
            .font(.system(size: 12, weight: .bold))
            .foregroundStyle(AppColor.onAccent)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(Capsule().fill(AppColor.accent))
    }
}

struct ParkingWidget: Widget {
    let kind = "ParkingWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: ParkingProvider()) { entry in
            ParkingWidgetView(entry: entry)
        }
        .configurationDisplayName("Car Locator")
        .description("Shows your latest saved parking spot.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}
