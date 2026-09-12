import Foundation

enum NavLinks {
    static func appleMapsURL(lat: Double, lng: Double) -> URL {
        URL(string: "http://maps.apple.com/?daddr=\(lat),\(lng)&dirflg=d")!
    }

    static func wazeURL(lat: Double, lng: Double) -> URL {
        URL(string: "waze://?ll=\(lat),\(lng)&navigate=yes")!
    }

    static func wazeWebURL(lat: Double, lng: Double) -> URL {
        URL(string: "https://waze.com/ul?ll=\(lat),\(lng)&navigate=yes")!
    }

    static func shareText(lat: Double, lng: Double) -> String {
        "My car is parked here: https://maps.google.com/?q=\(lat),\(lng)"
    }
}
