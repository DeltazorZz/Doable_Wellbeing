"use client";

import Navbar from "@/app/components/navbar";
import React, { useEffect, useMemo, useState } from "react";
import {
  AppointmentView,
  bookAppointmentFromSlot,
} from "@/lib/api/appointmentApi";
import {
  getCoachSlots,
  SlotView,
} from "@/lib/api/coachAvailabilityApi";
import { CoachSelect } from "@/app/components/booking/CoachSelect";
import {
  DateGrid,
  upcomingDays,
  toISODate,
} from "@/app/components/booking/DateGrid";
import { TimeSlotPicker } from "@/app/components/booking/TimeSlotPicker";
import { CrisisNotice } from "@/app/components/booking/CrisisNotice";
import { SessionOptions } from "@/app/components/booking/SessionOptions";
import { useCoaches } from "@/lib/hooks/useCoaches";

// --- Helper: time formatting ---
function fmtTime(iso: string) {
  const d = new Date(iso);
  return d.toLocaleTimeString(undefined, {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function AppointmentBookingPage() {
  // --- State ---

  const [selectedCoach, setSelectedCoach] = useState<string>("");
  const days = useMemo(() => upcomingDays(21), []);
  const [selectedDate, setSelectedDate] = useState<string>(
    toISODate(new Date())
  );
  const [slots, setSlots] = useState<SlotView[]>([]);
  const [pickedSlot, setPickedSlot] = useState<SlotView | null>(null);

  const [goal, setGoal] = useState("");
  const [notes, setNotes] = useState("");
  const [emailReminder, setEmailReminder] = useState(true);
  const [extraNotesEnabled, setExtraNotesEnabled] = useState(false);

  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const selectedDayUi = days.find((d) => d.iso === selectedDate);

  // Coach lista (custom hook)
  const { coaches } = useCoaches();

  // Ha még nincs kiválasztott coach, de már van lista, állítsuk be az elsőt
  useEffect(() => {
    if (!selectedCoach && coaches.length > 0) {
      setSelectedCoach(coaches[0].id);
    }
  }, [coaches, selectedCoach]);

  // Slotok betöltése coach + dátum alapján
  useEffect(() => {
    if (!selectedCoach || !selectedDate) return;

    setSlots([]);
    setPickedSlot(null);

    async function loadSlots() {
      try {
        // from = to = selectedDate, pl. 60 perces slotok
        const data = await getCoachSlots(
          selectedCoach,
          selectedDate,
          selectedDate,
          60
        );
        setSlots(data);
      } catch (err) {
        console.error("Failed to load slots", err);
        setSlots([]);
      }
    }

    loadSlots();
  }, [selectedCoach, selectedDate]);

  // Submit – slot-based booking
  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedCoach || !pickedSlot) return;

    setSubmitting(true);
    setMessage(null);

    try {
      const startDate = new Date(pickedSlot.start);
      const endDate = new Date(pickedSlot.end);
      const durationMinutes = Math.round(
        (endDate.getTime() - startDate.getTime()) / (60 * 1000)
      );

      const combinedNotesParts: string[] = [];
      if (goal.trim()) combinedNotesParts.push(`Goal: ${goal.trim()}`);
      if (extraNotesEnabled && notes.trim()) {
        combinedNotesParts.push(`Notes: ${notes.trim()}`);
      }
      combinedNotesParts.push(
        `Email reminder requested: ${emailReminder ? "yes" : "no"}`
      );
      const combinedNotes = combinedNotesParts.join("\n");

      const appt: AppointmentView = await bookAppointmentFromSlot({
        coachId: selectedCoach,
        slotStart: pickedSlot.start,
        durationMinutes,
        notes: combinedNotes || undefined,
      });

      setMessage(
        `Your session is booked for ${new Date(
          appt.startsAt
        ).toLocaleString()} (status: ${appt.status}).`
      );

      setGoal("");
      setNotes("");
      setExtraNotesEnabled(false);
      setPickedSlot(null);
    } catch (err) {
      console.error(err);
      setMessage("Something went wrong while booking. Please try again.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-4xl px-4 py-10">
        <h1 className="text-3xl font-bold tracking-tight text-black">
          Book a session
        </h1>
        <p className="mt-1 text-black">
          Let’s find a time that works for you
        </p>

        <CrisisNotice />

        <form onSubmit={onSubmit} className="mt-8 space-y-8">
          {/* Coach select */}
          <CoachSelect
            coaches={coaches}
            selectedCoach={selectedCoach}
            onChange={setSelectedCoach}
          />

          {/* Date picker grid */}
          <section>
            <h3 className="mb-3 text-sm font-semibold text-gray-800">
              Choose your date:
            </h3>
            <DateGrid
              days={days}
              selectedDate={selectedDate}
              onSelectDate={setSelectedDate}
            />
          </section>

          
          <section>
            <h3 className="mb-3 text-sm font-semibold text-gray-800">
              Available times{" "}
              {selectedDayUi ? `for ${selectedDayUi.label}` : ""}
            </h3>
            <TimeSlotPicker
              slots={slots}
              pickedSlot={pickedSlot}
              onPickSlot={setPickedSlot}
              fmtTime={fmtTime}
            />
          </section>

          {/* Session goal + options */}
          <SessionOptions
            goal={goal}
            setGoal={setGoal}
            notes={notes}
            setNotes={setNotes}
            emailReminder={emailReminder}
            setEmailReminder={setEmailReminder}
            extraNotesEnabled={extraNotesEnabled}
            setExtraNotesEnabled={setExtraNotesEnabled}
          />

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
              <span className="text-sm text-gray-500">
                Choose a time to continue.
              </span>
            )}
          </div>

          {message && (
            <div className="rounded-xl border border-gray-200 bg-gray-50 p-4 text-sm text-gray-800">
              {message}
            </div>
          )}
        </form>

        <footer className="mt-16 border-t pt-6 text-sm text-gray-500">
          © Doable Wellbeing
        </footer>
      </main>
    </>
  );
}
