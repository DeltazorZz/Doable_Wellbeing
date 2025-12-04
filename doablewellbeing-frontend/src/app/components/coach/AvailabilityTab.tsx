"use client";

import React from "react";
import { useCoachAvailability } from "@/lib/hooks/useCoachAvailability";
import { CoachAvailabilityResponse } from "@/lib/api/coachAvailabilityApi";

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

  const handleSubmit: React.FormEventHandler = async (e) => {
    e.preventDefault();
    await createAvailability();
  };

  return (
    <section className="max-w-4xl space-y-6 rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-200">
      {/* Header */}
      <header className="space-y-1">
        <h2 className="text-lg font-semibold text-slate-900">
          Your availability
        </h2>
        <p className="text-sm text-slate-500">
          Define when clients can book sessions with you. You can create
          single-day or recurring (weekly) time windows.
        </p>
      </header>

      {/* Date picker */}
      <div className="flex flex-wrap items-center gap-3">
        <label className="text-sm font-medium text-slate-700">
          Date
        </label>
        <input
          type="date"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          className="rounded-md border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-900 focus:border-teal-500 focus:outline-none focus:ring-1 focus:ring-teal-500"
        />
        <span className="text-xs text-slate-500">
          Editing availability for <span className="font-medium">{selectedDate}</span>
        </span>
      </div>

      {/* Current slots for selected date */}
      <div className="space-y-2">
        <h3 className="text-sm font-semibold text-slate-900">
          Time windows for this date
        </h3>

        {loading && (
          <p className="text-xs text-slate-500">Loading…</p>
        )}

        {!loading && availabilities.length === 0 && (
          <p className="text-xs text-slate-500">
            No availability defined for this date yet.
          </p>
        )}

        {!loading && availabilities.length > 0 && (
          <ul className="space-y-2">
            {availabilities.map((a: CoachAvailabilityResponse) => (
              <li
                key={a.id}
                className="flex items-center justify-between rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs"
              >
                <div className="space-y-0.5">
                  <div className="font-semibold text-slate-800">
                    {a.startTime.slice(0, 5)} – {a.endTime.slice(0, 5)}
                  </div>
                  <div className="flex flex-wrap items-center gap-2 text-[11px] text-slate-500">
                    {a.recurring && (
                      <span className="inline-flex rounded-full bg-emerald-50 px-2 py-0.5 font-medium text-emerald-700">
                        Repeats weekly
                      </span>
                    )}
                    {!a.active && (
                      <span className="inline-flex rounded-full bg-slate-200 px-2 py-0.5 font-medium text-slate-700">
                        Inactive
                      </span>
                    )}
                    <span>Series: {a.seriesId ?? "—"}</span>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => removeAvailability(a.id)}
                  className="text-[11px] font-medium text-red-500 hover:text-red-600"
                >
                  Remove
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* New availability form */}
      <form
        onSubmit={handleSubmit}
        className="mt-2 flex flex-col items-end gap-3 sm:flex-row sm:items-end"
      >
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-600">
            Start
          </label>
          <input
            type="time"
            value={form.startTime}
            onChange={(e) =>
              setForm((prev) => ({ ...prev, startTime: e.target.value }))
            }
            className="rounded-md border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-900 focus:border-teal-500 focus:outline-none focus:ring-1 focus:ring-teal-500"
          />
        </div>

        <div>
          <label className="mb-1 block text-xs font-medium text-slate-600">
            End
          </label>
          <input
            type="time"
            value={form.endTime}
            onChange={(e) =>
              setForm((prev) => ({ ...prev, endTime: e.target.value }))
            }
            className="rounded-md border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-900 focus:border-teal-500 focus:outline-none focus:ring-1 focus:ring-teal-500"
          />
        </div>

        <div className="flex items-center gap-2">
          <input
            id="recurring"
            type="checkbox"
            checked={form.recurring}
            onChange={(e) =>
              setForm((prev) => ({ ...prev, recurring: e.target.checked }))
            }
            className="h-4 w-4 rounded border-slate-300 text-teal-600 focus:ring-teal-500"
          />
          <label
            htmlFor="recurring"
            className="text-xs font-medium text-slate-700"
          >
            Repeat weekly from this date
          </label>
        </div>

        <button
          type="submit"
          className="rounded-md bg-teal-600 px-4 py-2 text-sm font-medium text-white hover:bg-teal-700"
        >
          Add window
        </button>
      </form>

      {/* Feedback */}
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
      {success && (
        <p className="mt-1 text-xs text-emerald-600">{success}</p>
      )}
    </section>
  );
}
