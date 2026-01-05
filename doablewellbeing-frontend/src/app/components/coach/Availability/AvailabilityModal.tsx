"use client";

import React from "react";
import { formatDayLabel } from "./calendarUtils";

type FormState = {
  startTime: string;
  endTime: string;
  recurring?: boolean;
  repeatWeeks?: number;
};

interface AvailabilityModalProps {
  open: boolean;
  onClose: () => void;

  selectedDate: string;

  form: FormState;
  setForm: React.Dispatch<React.SetStateAction<FormState>>;

  onSubmit: (e: React.FormEvent) => Promise<void> | void;
}

export function AvailabilityModal({
  open,
  onClose,
  selectedDate,
  form,
  setForm,
  onSubmit,
}: AvailabilityModalProps) {
  if (!open) return null;

  const timeInvalid = !form.startTime || !form.endTime || form.startTime >= form.endTime;
  const dayLabel = formatDayLabel(selectedDate);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-lg rounded-2xl bg-white p-5 shadow-xl">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-sm text-slate-500">Add availability</div>
            <div className="text-lg font-semibold text-slate-900">{dayLabel}</div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg px-2 py-1 text-slate-600 hover:bg-slate-100"
            aria-label="Close"
          >
            ✕
          </button>
        </div>

        <form
          onSubmit={onSubmit}
          className="mt-4 space-y-4"
        >
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-1 block text-xs font-medium text-slate-600">
                Start
              </label>
              <input
                type="time"
                value={form.startTime}
                onChange={(e) => setForm((p) => ({ ...p, startTime: e.target.value }))}
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-teal-500 focus:outline-none focus:ring-1 focus:ring-teal-500"
              />
            </div>

            <div>
              <label className="mb-1 block text-xs font-medium text-slate-600">
                End
              </label>
              <input
                type="time"
                value={form.endTime}
                onChange={(e) => setForm((p) => ({ ...p, endTime: e.target.value }))}
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-teal-500 focus:outline-none focus:ring-1 focus:ring-teal-500"
              />
            </div>
          </div>

          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input
              type="checkbox"
              checked={!!form.recurring}
              onChange={(e) => setForm((p) => ({ ...p, recurring: e.target.checked }))}
              className="h-4 w-4 rounded border-slate-300 text-teal-600 focus:ring-teal-500"
            />
            Repeat weekly
          </label>

          {form.recurring && (
            <div>
              <label className="mb-1 block text-xs font-medium text-slate-600">
                Repeat weeks
              </label>
              <input
                type="number"
                min={1}
                max={52}
                value={form.repeatWeeks ?? 8}
                onChange={(e) =>
                  setForm((p) => ({ ...p, repeatWeeks: Number(e.target.value || 8) }))
                }
                className="w-32 rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-teal-500 focus:outline-none focus:ring-1 focus:ring-teal-500"
              />
            </div>
          )}

          {timeInvalid && (
            <p className="text-sm text-amber-700">
              Please select a valid time range (end must be after start).
            </p>
          )}

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={timeInvalid}
              className="rounded-xl bg-teal-600 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Add
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
