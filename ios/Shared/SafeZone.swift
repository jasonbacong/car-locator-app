import Foundation
import SwiftData

/// A named location, in addition to the home zone, where auto-save should stay quiet.
@Model
final class SafeZone {
    var name: String
    var latitude: Double
    var longitude: Double
    var radiusMeters: Double

    init(name: String, latitude: Double, longitude: Double, radiusMeters: Double) {
        self.name = name
        self.latitude = latitude
        self.longitude = longitude
        self.radiusMeters = radiusMeters
    }
}
