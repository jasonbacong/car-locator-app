import SwiftUI
import SwiftData

struct SafeZonesSection: View {
    let zones: [SafeZone]

    @Environment(\.modelContext) private var modelContext
    @State private var showAddSheet = false

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionEyebrow(text: "Other safe zones")
            SectionCard {
                if zones.isEmpty {
                    Text("No other safe zones — auto-save only skips near home.")
                        .font(AppFont.body())
                        .foregroundStyle(AppColor.textSecondary)
                } else {
                    ForEach(zones) { zone in
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(zone.name)
                                    .font(AppFont.rowTitle())
                                    .foregroundStyle(AppColor.textPrimary)
                                Text("\(Int(zone.radiusMeters)) m radius")
                                    .font(AppFont.mono(12))
                                    .foregroundStyle(AppColor.textSecondary)
                            }
                            Spacer()
                            TactileButton(title: "Remove", variant: .danger) {
                                modelContext.delete(zone)
                                try? modelContext.save()
                            }
                        }
                    }
                }
                TactileButton(title: "Add safe zone", variant: .secondary, fullWidth: true) {
                    showAddSheet = true
                }
            }
        }
        .sheet(isPresented: $showAddSheet) {
            AddZoneSheet { name, lat, lng, radius in
                modelContext.insert(SafeZone(name: name, latitude: lat, longitude: lng, radiusMeters: radius))
                try? modelContext.save()
                showAddSheet = false
            }
        }
    }
}

struct AddZoneSheet: View {
    let onAdd: (String, Double, Double, Double) -> Void

    @Environment(\.dismiss) private var dismiss
    @StateObject private var locationService = ObservableLocationService()

    @State private var name = ""
    @State private var capturedLat: Double?
    @State private var capturedLng: Double?
    @State private var radius: Double = 150
    @State private var statusText: String?

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                TextField("Name, e.g. Work", text: $name)
                    .textFieldStyle(.roundedBorder)

                TactileButton(title: "Use current location", variant: .secondary, fullWidth: true) {
                    Task {
                        locationService.requestWhenInUseAuthorizationIfNeeded()
                        if let location = await locationService.currentLocation() {
                            capturedLat = location.coordinate.latitude
                            capturedLng = location.coordinate.longitude
                            statusText = String(format: "%.5f, %.5f", location.coordinate.latitude, location.coordinate.longitude)
                        } else {
                            statusText = "Couldn't get a location fix."
                        }
                    }
                }

                if let statusText {
                    Text(statusText)
                        .font(AppFont.mono(12))
                        .foregroundStyle(AppColor.textSecondary)
                }

                Text("\(Int(radius)) m radius")
                    .font(AppFont.caption())
                    .foregroundStyle(AppColor.textSecondary)
                Slider(value: $radius, in: 50...500)
                    .tint(AppColor.accent)

                Spacer()
            }
            .padding(20)
            .background(AppColor.background)
            .navigationTitle("Add safe zone")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add") {
                        if let lat = capturedLat, let lng = capturedLng, !name.trimmingCharacters(in: .whitespaces).isEmpty {
                            onAdd(name.trimmingCharacters(in: .whitespaces), lat, lng, radius)
                        }
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty || capturedLat == nil || capturedLng == nil)
                }
            }
        }
    }
}
