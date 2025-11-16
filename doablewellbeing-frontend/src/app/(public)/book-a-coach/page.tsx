"use client";
import Navbar from "@/app/components/navbar";
import React, { useEffect, useMemo, useState } from "react";

type Coach = {
  id: string;
  name: string;
};

type TimeSlot = {
  start: string; // ISO string
  end: string; // ISO string
};

// --- Mock data fetchers (replace with real API calls) ---
async function fetchCoaches(): Promise<Coach[]> {
  // TODO: GET /api/coaches
  await new Promise((r) => setTimeout(r, 250));
  return [
    { id: "c1", name: "Chris Lynas" },
    { id: "c2", name: "Jane Smith" },
  ];
}

async function fetchTimeSlots(coachId: string, dayISO: string): Promise<TimeSlot[]> {
  // TODO: GET /api/coaches/{coachId}/availability?date=YYYY-MM-DD
  await new Promise((r) => setTimeout(r, 300));
  // Demo: generate half‑hour slots 09:00–12:00 local
  const base = new Date(dayISO + "T09:00:00");
  const slots: TimeSlot[] = [];
  for (let i = 0; i < 6; i++) {
    const start = new Date(base.getTime() + i * 30 * 60 * 1000);
    const end = new Date(start.getTime() + 30 * 60 * 1000);
    slots.push({ start: start.toISOString(), end: end.toISOString() });
  }
  return slots;
}

// --- Helpers ---
function fmtTime(iso: string) {
  const d = new Date(iso);
  return d.toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" });
}

function toISODate(d: Date) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${dd}`;
}

function upcomingDays(days = 14) {
  const arr: { date: Date; iso: string; label: string; dow: string }[] = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  for (let i = 0; i < days; i++) {
    const d = new Date(today);
    d.setDate(today.getDate() + i);
    const iso = toISODate(d);
    const dow = d.toLocaleDateString(undefined, { weekday: "short" });
    const label = d.toLocaleDateString(undefined, { month: "short", day: "numeric" });
    arr.push({ date: d, iso, label, dow });
  }
  return arr;
}

export default function AppointmentBookingPage() {
  // --- State ---
  const [coaches, setCoaches] = useState<Coach[]>([]);
  const [selectedCoach, setSelectedCoach] = useState<string>("");
  const days = useMemo(() => upcomingDays(21), []);
  const [selectedDate, setSelectedDate] = useState<string>(toISODate(new Date()));
  const [slots, setSlots] = useState<TimeSlot[]>([]);
  const [pickedSlot, setPickedSlot] = useState<TimeSlot | null>(null);
  const [goal, setGoal] = useState("");
  const [notes, setNotes] = useState("");
  const [emailReminder, setEmailReminder] = useState(true);
  const [extraNotesEnabled, setExtraNotesEnabled] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  // Load coaches
  useEffect(() => {
    fetchCoaches().then((c) => {
      setCoaches(c);
      if (c.length && !selectedCoach) setSelectedCoach(c[0].id);
    });
  }, []);

  // Load slots when coach/date changes
  useEffect(() => {
    if (!selectedCoach || !selectedDate) return;
    setSlots([]);
    setPickedSlot(null);
    fetchTimeSlots(selectedCoach, selectedDate)
      .then(setSlots)
      .catch(() => setSlots([]));
  }, [selectedCoach, selectedDate]);

  const selectedDayUi = days.find((d) => d.iso === selectedDate);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedCoach || !pickedSlot) return;
    setSubmitting(true);
    setMessage(null);
    try {
      // TODO: POST /api/appointments
      // Example payload
      const payload = {
        coachId: selectedCoach,
        startsAt: pickedSlot.start,
        endsAt: pickedSlot.end,
        goal,
        notes: extraNotesEnabled ? notes : undefined,
        emailReminder,
      };
      // eslint-disable-next-line no-console
      console.log("Booking payload", payload);
      await new Promise((r) => setTimeout(r, 600));
      setMessage("Your session is booked! Check your email for details.");
      setGoal("");
      setNotes("");
      setExtraNotesEnabled(false);
      setPickedSlot(null);
    } catch (err) {
      setMessage("Something went wrong while booking. Please try again.");
    } finally {
      setSubmitting(false);
    }
  }

  return (<>
    <Navbar />
    <main className="mx-auto max-w-4xl px-4 py-10">

      <h1 className="text-3xl font-bold tracking-tight text-black">Book a session</h1>
      <p className="mt-1 text-black">Let’s find a time that works for you</p>

      {/* Crisis notice from design notes */}
      <div className="mt-6 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
        <strong>Your safety is our priority.</strong> Doable Wellbeing is not a crisis service. If you feel you may harm yourself, call the Samaritans at <span className="whitespace-nowrap">116 123</span>, call <span className="whitespace-nowrap">999</span>, or seek help at A&E.
      </div>

      <form onSubmit={onSubmit} className="mt-8 space-y-8">
        {/* Coach select */}
        <div>
          <label htmlFor="coach" className="mb-2 block text-sm font-medium text-gray-900">
            With (Coach)
          </label>
          <div className="relative w-full">
            <select
              id="coach"
              value={selectedCoach}
              onChange={(e) => setSelectedCoach(e.target.value)}
              className="w-full appearance-none rounded-xl border border-gray-300 bg-white px-4 py-3 pr-10 text-gray-900 shadow-sm outline-none ring-0 transition focus:border-gray-900"
              >
              {coaches.length === 0 && <option>Loading…</option>}
              {coaches.map((c) => (
                  <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
            <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">▾</span>
          </div>
        </div>

        {/* Date picker grid (low‑fi style) */}
        <section>
          <h3 className="mb-3 text-sm font-semibold text-gray-800">Choose your date:</h3>
          <div className="grid grid-cols-4 gap-2 sm:grid-cols-7">
            {days.map((d) => {
              const isSelected = d.iso === selectedDate;
              return (
                  <button
                  key={d.iso}
                  type="button"
                  onClick={() => setSelectedDate(d.iso)}
                  className={
                      "flex flex-col items-center rounded-lg border px-3 py-2 text-sm transition " +
                      (isSelected
                        ? "border-gray-900 bg-gray-900 text-white"
                        : "border-gray-200 bg-gray-50 text-gray-800 hover:border-gray-400")
                    }
                    aria-pressed={isSelected}
                >
                  <span className="text-xs opacity-70">{d.dow}</span>
                  <span className="font-medium">{d.label}</span>
                </button>
              );
            })}
          </div>
        </section>

        {/* Available times */}
        <section>
          <h3 className="mb-3 text-sm font-semibold text-gray-800">
            Available times {selectedDayUi ? `for ${selectedDayUi.label}` : ""}
          </h3>
          {slots.length === 0 ? (
              <div className="rounded-xl border border-dashed border-gray-300 p-6 text-center text-gray-500">
              {selectedCoach ? "Loading times…" : "Select a coach"}
            </div>
          ) : (
              <div className="flex flex-wrap gap-2">
              {slots.map((s) => {
                  const isPicked = pickedSlot?.start === s.start;
                  return (
                      <button
                      key={s.start}
                      type="button"
                      onClick={() => setPickedSlot(s)}
                      className={
                          "rounded-lg border px-4 py-2 text-sm transition " +
                          (isPicked
                            ? "border-gray-900 bg-gray-900 text-white"
                            : "border-gray-200 bg-white text-gray-900 hover:border-gray-400")
                        }
                        aria-pressed={isPicked}
                        >
                    {fmtTime(s.start)} – {fmtTime(s.end)}
                  </button>
                );
            })}
            </div>
          )}
        </section>

        {/* Goal */}
        <section>
          <label htmlFor="goal" className="mb-2 block text-sm font-semibold text-gray-800">
            Do you have a goal for this session? Tell us!
          </label>
          <textarea
            id="goal"
            value={goal}
            onChange={(e) => setGoal(e.target.value)}
            rows={4}
            placeholder="e.g., I want to prepare for a stressful presentation next week."
            className="w-full resize-y rounded-xl border border-gray-300 bg-white p-3 shadow-sm outline-none transition focus:border-gray-900"
            />
        </section>

        {/* Options */}
        <section className="space-y-4">
          <label className="flex items-center gap-3 text-gray-800">
            <input
              type="checkbox"
              className="h-5 w-5 rounded border-gray-300 text-gray-900 focus:ring-gray-900"
              checked={emailReminder}
              onChange={(e) => setEmailReminder(e.target.checked)}
              />
            <span>Send me an email reminder</span>
          </label>

          <label className="flex items-center gap-3 text-gray-800">
            <input
              type="checkbox"
              className="h-5 w-5 rounded border-gray-300 text-gray-900 focus:ring-gray-900"
              checked={extraNotesEnabled}
              onChange={(e) => setExtraNotesEnabled(e.target.checked)}
              />
            <span>Add extra notes (optional)</span>
          </label>

          {extraNotesEnabled && (
            <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            rows={3}
            placeholder="Anything else you'd like your coach to know?"
            className="mt-2 w-full resize-y rounded-xl border border-gray-300 bg-white p-3 shadow-sm outline-none transition focus:border-gray-900"
            />
        )}
        </section>

        {/* Submit */}
        <div className="flex items-center gap-3">
          <button
            type="submit"
            disabled={submitting || !selectedCoach || !pickedSlot}
            className="inline-flex items-center justify-center rounded-xl bg-gray-900 px-5 py-3 text-white shadow-sm transition hover:bg-black disabled:cursor-not-allowed disabled:opacity-50"
            >
            {submitting ? "Booking…" : "Book session"}
          </button>
          {!pickedSlot && (
              <span className="text-sm text-gray-500">Choose a time to continue.</span>
          )}
        </div>

        {message && (
            <div className="rounded-xl border border-gray-200 bg-gray-50 p-4 text-sm text-gray-800">
            {message}
          </div>
        )}
      </form>

      {/* Footer placeholder */}
      <footer className="mt-16 border-t pt-6 text-sm text-gray-500">© Doable Wellbeing</footer>
    </main>
    </>
  );
}
