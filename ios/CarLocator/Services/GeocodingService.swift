import CoreLocation

enum GeocodingService {
    static func reverseGeocode(_ location: CLLocation) async -> String? {
        let geocoder = CLGeocoder()
        do {
            let placemarks = try await geocoder.reverseGeocodeLocation(location)
            guard let placemark = placemarks.first else { return nil }
            let parts = [placemark.subThoroughfare, placemark.thoroughfare, placemark.locality]
                .compactMap { $0 }
            return parts.isEmpty ? nil : parts.joined(separator: " ")
        } catch {
            return nil
        }
    }
}
