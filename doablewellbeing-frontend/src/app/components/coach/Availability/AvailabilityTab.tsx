"use client";

import React, { useMemo, useState } from "react";
import { useCoachAvailability } from "@/lib/hooks/useCoachAvailability";
import { AvailabilityCalendar } from "./AvailabilityCalendar";
import { AvailabilityDayPanel } from "./AvailabilityDayPanel";
import { AvailabilityModal } from "./AvailabilityModal";

export function AvailabilityTab() {
  const {
    selectedDate,
    setSelectedDate,
    availabilities,
    loading,
    error,
    success,
    form,
    setForm,
    createAvailability,
    removeAvailability,
  } = useCoachAvailability();

  const [monthAnchor, setMonthAnchor] = useState(() => {
    const d = new Date(selectedDate);
    if (isNaN(d.getTime())) return new Date();
    return new Date(d.getFullYear(), d.getMonth(), 1);
  });

  const [open, setOpen] = useState(false);
  const [removingId, setRemovingId] = useState<string | null>(null);

  const onPrevMonth = () =>
    setMonthAnchor(new Date(monthAnchor.getFullYear(), monthAnchor.getMonth() - 1, 1));
  const onNextMonth = () =>
    setMonthAnchor(new Date(monthAnchor.getFullYear(), monthAnchor.getMonth() + 1, 1));

  // NOTE: later month summary dots; for now only selected day can show dot if it has windows
  const hasDotForDate = (iso: string) => iso === selectedDate && availabilities.some((a) => a.active);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    await createAvailability();
    setOpen(false);
  }

  async function onRemove(id: string) {
    try {
      setRemovingId(id);
      await removeAvailability(id);
    } finally {
      setRemovingId(null);
    }
  }

  return (
    <section className="max-w-6xl space-y-6">
      {/* Header + feedback */}
      <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
        <h2 className="text-xl font-semibold text-slate-900">Availability</h2>
        <p className="mt-1 text-sm text-slate-500">
          Pick a day, then add time windows clients can book.
        </p>

        {(error || success) && (
          <div
            className={
              "mt-3 rounded-lg border px-3 py-2 text-sm " +
              (error
                ? "border-red-200 bg-red-50 text-red-700"
                : "border-emerald-200 bg-emerald-50 text-emerald-700")
            }
          >
            {error ?? success}
          </div>
        )}
      </div>

      <div className="grid gap-6 lg:grid-cols-[360px_1fr]">
        <AvailabilityCalendar
          monthAnchor={monthAnchor}
          onPrevMonth={onPrevMonth}
          onNextMonth={onNextMonth}
          selectedDate={selectedDate}
          onSelectDate={setSelectedDate}
          hasDotForDate={hasDotForDate}
        />

        <AvailabilityDayPanel
          selectedDate={selectedDate}
          loading={loading}
          availabilities={availabilities}
          onAdd={() => setOpen(true)}
          onRemove={onRemove}
          removingId={removingId}
        />
      </div>

      <AvailabilityModal
        open={open}
        onClose={() => setOpen(false)}
        selectedDate={selectedDate}
        form={form as any}
        setForm={setForm as any}
        onSubmit={onSubmit}
      />
    </section>
  );
}
