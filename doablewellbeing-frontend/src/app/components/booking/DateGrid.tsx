"use client";

export function toISODate(d: Date) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${dd}`;
}

export function upcomingDays(days = 14) {
  const arr: { date: Date; iso: string; label: string; dow: string }[] = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  for (let i = 0; i < days; i++) {
    const d = new Date(today);
    d.setDate(today.getDate() + i);
    const iso = toISODate(d);
    const dow = d.toLocaleDateString(undefined, { weekday: "short" });
    const label = d.toLocaleDateString(undefined, {
      month: "short",
      day: "numeric",
    });
    arr.push({ date: d, iso, label, dow });
  }
  return arr;
}

interface DateGridProps {
  days: { date: Date; iso: string; label: string; dow: string }[];
  selectedDate: string;
  onSelectDate: (iso: string) => void;
}

export function DateGrid({ days, selectedDate, onSelectDate }: DateGridProps) {
  return (
    <div className="grid grid-cols-4 gap-2 sm:grid-cols-7">
      {days.map((d) => {
        const isSelected = d.iso === selectedDate;
        return (
          <button
            key={d.iso}
            type="button"
            onClick={() => onSelectDate(d.iso)}
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
  );
}
