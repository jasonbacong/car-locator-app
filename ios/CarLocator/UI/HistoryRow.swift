import SwiftUI

struct HistoryRow: View {
    let spot: ParkingSpot
    let onDelete: () -> Void
    let onLocate: () -> Void
    let onSaveNote: (String?) -> Void
    let onScheduleReminder: (Double) -> Void

    @State private var showNoteSheet = false
    @State private var showReminderSheet = false

    private var dateText: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, HH:mm"
        return formatter.string(from: spot.timestamp)
    }

    var body: some View {
        SectionCard {
            HStack(alignment: .center, spacing: 12) {
                ParkingBadge(size: 40)
                VStack(alignment: .leading, spacing: 2) {
                    Text(spot.address ?? String(format: "%.5f, %.5f", spot.latitude, spot.longitude))
                        .font(spot.address != nil ? AppFont.rowTitle() : AppFont.mono(16))
                        .foregroundStyle(AppColor.textPrimary)
                    Text(dateText)
                        .font(AppFont.mono(12))
                        .foregroundStyle(AppColor.textSecondary)
                }
                Spacer(minLength: 0)
            }

            if let note = spot.note {
                Text("Note: \(note)")
                    .font(AppFont.body())
                    .foregroundStyle(AppColor.textSecondary)
            }

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    TactileButton(title: "Maps", variant: .secondary) {
                        UIApplication.shared.open(NavLinks.appleMapsURL(lat: spot.latitude, lng: spot.longitude))
                    }
                    TactileButton(title: "Waze", variant: .secondary) {
                        let url = NavLinks.wazeURL(lat: spot.latitude, lng: spot.longitude)
                        if UIApplication.shared.canOpenURL(url) {
                            UIApplication.shared.open(url)
                        } else {
                            UIApplication.shared.open(NavLinks.wazeWebURL(lat: spot.latitude, lng: spot.longitude))
                        }
                    }
                    TactileButton(title: "Locate", variant: .secondary, action: onLocate)
                    ShareLink(item: NavLinks.shareText(lat: spot.latitude, lng: spot.longitude)) {
                        Text("Share")
                            .font(AppFont.body().bold())
                            .foregroundStyle(AppColor.textPrimary)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 10)
                            .background(Capsule().fill(AppColor.surface).overlay(Capsule().stroke(AppColor.border, lineWidth: 1)))
                    }
                    TactileButton(title: "Remind", variant: .secondary) { showReminderSheet = true }
                    TactileButton(title: spot.note == nil ? "Add note" : "Edit note", variant: .secondary) {
                        showNoteSheet = true
                    }
                    TactileButton(title: "Delete", variant: .danger, action: onDelete)
                }
            }
        }
        .sheet(isPresented: $showNoteSheet) {
            NoteSheet(initialNote: spot.note) { note in
                onSaveNote(note)
                showNoteSheet = false
            }
        }
        .sheet(isPresented: $showReminderSheet) {
            ReminderSheet { minutes in
                onScheduleReminder(minutes)
                showReminderSheet = false
            }
        }
    }
}

struct NoteSheet: View {
    let initialNote: String?
    let onSave: (String?) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var text: String

    init(initialNote: String?, onSave: @escaping (String?) -> Void) {
        self.initialNote = initialNote
        self.onSave = onSave
        _text = State(initialValue: initialNote ?? "")
    }

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                TextField("e.g. Level 3, Section B", text: $text)
                    .textFieldStyle(.roundedBorder)
                    .padding(.top, 16)
                Spacer()
            }
            .padding(20)
            .background(AppColor.background)
            .navigationTitle("Note")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
                        onSave(trimmed.isEmpty ? nil : trimmed)
                    }
                }
            }
        }
        .presentationDetents([.height(180)])
    }
}

struct ReminderSheet: View {
    let onSchedule: (Double) -> Void
    @Environment(\.dismiss) private var dismiss

    private let durations: [(String, Double)] = [
        ("30 minutes", 30), ("1 hour", 60), ("2 hours", 120), ("4 hours", 240)
    ]

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                ForEach(durations, id: \.1) { label, minutes in
                    TactileButton(title: label, variant: .secondary, fullWidth: true) {
                        onSchedule(minutes)
                    }
                }
            }
            .padding(20)
            .background(AppColor.background)
            .navigationTitle("Remind me in…")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
        .presentationDetents([.height(280)])
    }
}
