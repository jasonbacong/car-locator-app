import Foundation
import SwiftData

/// Plain synchronous data access usable from anywhere: the App Intent (its own
/// process context), the widget's TimelineProvider, and the notification delegate —
/// none of which have a SwiftUI `@Environment(\.modelContext)` to draw from.
/// The main app's own screens use `@Query`/`@Environment` directly instead, since
/// that gives free reactive updates there.
enum ParkingRepository {
    private static func newContext() -> ModelContext {
        ModelContext(PersistenceController.shared)
    }

    @discardableResult
    static func save(latitude: Double, longitude: Double, address: String?) -> ParkingSpot {
        let context = newContext()
        let spot = ParkingSpot(timestamp: .now, latitude: latitude, longitude: longitude, address: address)
        context.insert(spot)
        try? context.save()
        cleanupOldSpots(context: context)
        return spot
    }

    static func deleteSpot(withTimestamp timestamp: TimeInterval) {
        let context = newContext()
        let target = Date(timeIntervalSince1970: timestamp)
        let descriptor = FetchDescriptor<ParkingSpot>(predicate: #Predicate { $0.timestamp == target })
        if let matches = try? context.fetch(descriptor) {
            for spot in matches { context.delete(spot) }
            try? context.save()
        }
    }

    static func updateNote(timestamp: TimeInterval, note: String?) {
        let context = newContext()
        let target = Date(timeIntervalSince1970: timestamp)
        let descriptor = FetchDescriptor<ParkingSpot>(predicate: #Predicate { $0.timestamp == target })
        if let spot = try? context.fetch(descriptor).first {
            spot.note = note
            try? context.save()
        }
    }

    static func latestSpot() -> ParkingSpot? {
        let context = newContext()
        var descriptor = FetchDescriptor<ParkingSpot>(sortBy: [SortDescriptor(\.timestamp, order: .reverse)])
        descriptor.fetchLimit = 1
        return try? context.fetch(descriptor).first
    }

    static func safeZones() -> [SafeZone] {
        let context = newContext()
        return (try? context.fetch(FetchDescriptor<SafeZone>())) ?? []
    }

    private static func cleanupOldSpots(context: ModelContext) {
        let cutoff = Calendar.current.date(byAdding: .day, value: -60, to: .now) ?? .distantPast
        let descriptor = FetchDescriptor<ParkingSpot>(predicate: #Predicate { $0.timestamp < cutoff })
        if let old = try? context.fetch(descriptor) {
            for spot in old { context.delete(spot) }
            try? context.save()
        }
    }
}
