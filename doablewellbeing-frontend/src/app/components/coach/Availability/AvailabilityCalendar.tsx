"use client";

import React, { useMemo } from "react";
import { buildMonthGrid, formatMonthLabel } from "./calendarUtils";

interface AvailabilityCalendarProps {
  monthAnchor: Date;
  onPrevMonth: () => void;
  onNextMonth: () => void;

  selectedDate: string;
  onSelectDate: (iso: string) => void;


  hasDotForDate?: (iso: string) => boolean;
}

export function AvailabilityCalendar({
  monthAnchor,
  onPrevMonth,
  onNextMonth,
  selectedDate,
  onSelectDate,
  hasDotForDate,
}: AvailabilityCalendarProps) {
  const grid = useMemo(() => buildMonthGrid(monthAnchor), [monthAnchor]);
  const monthLabel = useMemo(() => formatMonthLabel(monthAnchor), [monthAnchor]);

  return (
    <div className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
      <div className="flex items-center justify-between">
        <button
          type="button"
          onClick={onPrevMonth}
          className="rounded-lg px-2 py-1 text-sm text-slate-600 hover:bg-slate-100"
        >
          ←
        </button>
        <div className="text-sm font-semibold text-slate-900">{monthLabel}</div>
        <button
          type="button"
          onClick={onNextMonth}
          className="rounded-lg px-2 py-1 text-sm text-slate-600 hover:bg-slate-100"
        >
          →
        </button>
      </div>

      <div className="mt-3 grid grid-cols-7 gap-1 text-xs text-slate-500">
        {["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map((d) => (
          <div key={d} className="px-2 py-1">
            {d}
          </div>
        ))}
      </div>

      <div className="mt-1 grid grid-cols-7 gap-1">
        {grid.map((cell) => {
          const isSelected = cell.iso === selectedDate;
          const dot = hasDotForDate?.(cell.iso);

          return (
            <button
              key={cell.iso}
              type="button"
              onClick={() => onSelectDate(cell.iso)}
              className={
                "relative rounded-xl px-2 py-2 text-left text-sm transition " +
                (isSelected
                  ? "bg-slate-900 text-white"
                  : "hover:bg-slate-100 " +
                    (cell.inMonth ? "text-slate-900" : "text-slate-400"))
              }
            >
              <div className="flex items-center justify-between">
                <span className="font-medium">{cell.day}</span>
                {dot && <span className="h-2 w-2 rounded-full bg-emerald-400" />}
              </div>
            </button>
          );
        })}
      </div>

      <div className="mt-4 text-xs text-slate-500">
        Tip: later we can add dots for every day that has availability.
      </div>
    </div>
  );
}
