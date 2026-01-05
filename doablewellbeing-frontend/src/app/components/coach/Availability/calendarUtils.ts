export type DayCell = {
  iso: string; 
  day: number;
  inMonth: boolean;
};

export function toISODate(d: Date) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const da = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${da}`;
}

export function startOfMonth(d: Date) {
  return new Date(d.getFullYear(), d.getMonth(), 1);
}

export function daysInMonth(d: Date) {
  return new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();
}

export function buildMonthGrid(anchor: Date): DayCell[] {
  const first = startOfMonth(anchor);
  const firstWeekday = (first.getDay() + 6) % 7; // Mon=0
  const totalDays = daysInMonth(anchor);

  const cells: DayCell[] = [];

  
  const prevMonth = new Date(anchor.getFullYear(), anchor.getMonth() - 1, 1);
  const prevTotal = daysInMonth(prevMonth);

  for (let i = firstWeekday - 1; i >= 0; i--) {
    const day = prevTotal - i;
    const date = new Date(anchor.getFullYear(), anchor.getMonth() - 1, day);
    cells.push({ iso: toISODate(date), day, inMonth: false });
  }

  
  for (let day = 1; day <= totalDays; day++) {
    const date = new Date(anchor.getFullYear(), anchor.getMonth(), day);
    cells.push({ iso: toISODate(date), day, inMonth: true });
  }

  
  while (cells.length < 42) {
    const nextDay = cells.length - (firstWeekday + totalDays) + 1;
    const date = new Date(anchor.getFullYear(), anchor.getMonth() + 1, nextDay);
    cells.push({ iso: toISODate(date), day: nextDay, inMonth: false });
  }

  return cells;
}

export function formatMonthLabel(d: Date) {
  return d.toLocaleDateString(undefined, { month: "long", year: "numeric" });
}

export function formatDayLabel(iso: string) {
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  return d.toLocaleDateString(undefined, {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

export function hhmm(t: string) {
  return t?.slice(0, 5) ?? "";
}
