"use client";

import React from "react";
import { CoachAvailabilityResponse } from "@/lib/api/coachAvailabilityApi";
import { formatDayLabel, hhmm } from "./calendarUtils";

interface AvailabilityDayPanelProps {
  selectedDate: string;

  loading: boolean;
  availabilities: CoachAvailabilityResponse[];

  onAdd: () => void;
  onRemove: (id: string) => void;

  removingId?: string | null;
}

export function AvailabilityDayPanel({
  selectedDate,
  loading,
  availabilities,
  onAdd,
  onRemove,
  removingId,
}: AvailabilityDayPanelProps) {
  const dayLabel = formatDayLabel(selectedDate);

  return (
    <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm text-slate-500">Selected day</div>
          <div className="text-lg font-semibold text-slate-900">{dayLabel}</div>
        </div>

        <button
          type="button"
          onClick={onAdd}
          className="rounded-xl bg-teal-600 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-700"
        >
          + Add availability
        </button>
      </div>

      <div className="mt-5">
        <div className="text-sm font-semibold text-slate-900">Time windows</div>

        {loading && <div className="mt-2 text-sm text-slate-500">Loading…</div>}

        {!loading && availabilities.length === 0 && (
          <div className="mt-2 rounded-xl border border-dashed border-slate-200 p-4 text-sm text-slate-500">
            No windows for this day yet.
          </div>
        )}

        {!loading && availabilities.length > 0 && (
          <div className="mt-3 space-y-2">
            {availabilities.map((a) => {
              const isRemoving = removingId === a.id;
              return (
                <div
                  key={a.id}
                  className="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50 px-4 py-3"
                >
                  <div>
                    <div className="text-sm font-semibold text-slate-900">
                      {hhmm(a.startTime)} – {hhmm(a.endTime)}
                    </div>
                    <div className="mt-1 flex flex-wrap gap-2 text-xs text-slate-500">
                      {a.recurring && (
                        <span className="rounded-full bg-emerald-50 px-2 py-0.5 font-medium text-emerald-700">
                          Weekly
                        </span>
                      )}
                      {!a.active && (
                        <span className="rounded-full bg-slate-200 px-2 py-0.5 font-medium text-slate-700">
                          Inactive
                        </span>
                      )}
                    </div>
                  </div>

                  <button
                    type="button"
                    onClick={() => {
                      const ok = confirm("Remove this availability window?");
                      if (ok) onRemove(a.id);
                    }}
                    disabled={isRemoving}
                    className="rounded-lg px-3 py-1.5 text-sm font-semibold text-red-600 hover:bg-red-50 disabled:opacity-50"
                  >
                    {isRemoving ? "Removing…" : "Remove"}
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
