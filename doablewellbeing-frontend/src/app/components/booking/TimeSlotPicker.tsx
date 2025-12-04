"use client";

import type { SlotView } from "@/lib/api/coachAvailabilityApi";

interface TimeSlotPickerProps {
  slots: SlotView[];
  pickedSlot: SlotView | null;
  onPickSlot: (slot: SlotView | null) => void;
  fmtTime: (iso: string) => string;
}


export function TimeSlotPicker({
  slots,
  pickedSlot,
  onPickSlot,
  fmtTime,
}: TimeSlotPickerProps) {

  if (slots.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-gray-300 p-6 text-center text-gray-500">
        Loading times or no available slots for this day.
      </div>
    );
  }

  return (
    <div className="flex flex-wrap gap-2">
      {slots.map((s) => {
        const isPicked = pickedSlot?.start === s.start;
        return (
          <button
            key={s.start}
            type="button"
            onClick={() => onPickSlot(s)}
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
  );
}
