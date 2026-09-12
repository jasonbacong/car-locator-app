import Foundation
import SwiftData

@Model
final class ParkingSpot {
    var timestamp: Date
    var latitude: Double
    var longitude: Double
    var address: String?
    var note: String?

    init(
        timestamp: Date = .now,
        latitude: Double,
        longitude: Double,
        address: String? = nil,
        note: String? = nil
    ) {
        self.timestamp = timestamp
        self.latitude = latitude
        self.longitude = longitude
        self.address = address
        self.note = note
    }
}
