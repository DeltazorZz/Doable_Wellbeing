"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  CoachAvailabilityResponse,
  createCoachAvailability,
  deleteMyCoachAvailability,
  getMyCoachAvailabilities,
} from "@/lib/api/coachAvailabilityApi";

import type {
  AvailabilityFormState,
  AvailabilityDaySummary,
} from "@/app/components/coach/Availability/types";

// ---------- date helpers (no deps) ----------
function pad2(n: number) {
  return String(n).padStart(2, "0");
}

function toISODate(d: Date) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;
}

function startOfMonth(year: number, monthIndex0: number) {
  return new Date(year, monthIndex0, 1);
}

function endOfMonth(year: number, monthIndex0: number) {
  // last day of month
  return new Date(year, monthIndex0 + 1, 0);
}

function addDays(d: Date, days: number) {
  const x = new Date(d);
  x.setDate(x.getDate() + days);
  return x;
}

/**
 * Calendar grid range: Monday -> Sunday (common EU UX)
 * Returns {fromISO, toISO} for fetching.
 */
function monthGridRangeISO(year: number, monthIndex0: number) {
  const first = startOfMonth(year, monthIndex0);
  const last = endOfMonth(year, monthIndex0);

  // JS: 0=Sun..6=Sat. We want Monday=0..Sunday=6 in our logic.
  const firstDow = (first.getDay() + 6) % 7; // Monday=0
  const lastDow = (last.getDay() + 6) % 7;   // Monday=0

  const gridStart = addDays(first, -firstDow);
  const gridEnd = addDays(last, 6 - lastDow);

  return { fromISO: toISODate(gridStart), toISO: toISODate(gridEnd) };
}

function isTimeStrValidHHmm(s: string) {
  // accepts "HH:mm" (input type="time" often gives "HH:mm")
  if (!/^\d{2}:\d{2}$/.test(s)) return false;
  const [hh, mm] = s.split(":").map(Number);
  return hh >= 0 && hh <= 23 && mm >= 0 && mm <= 59;
}

function timeToMinutes(hhmm: string) {
  const [hh, mm] = hhmm.split(":").map(Number);
  return hh * 60 + mm;
}

// ---------- hook ----------
export function useCoachAvailabilityCalendar() {
  // month view state
  const todayISO = useMemo(() => toISODate(new Date()), []);
  const [selectedDate, setSelectedDate] = useState<string>(todayISO);

  const initial = useMemo(() => {
    const d = new Date();
    return { year: d.getFullYear(), monthIndex0: d.getMonth() };
  }, []);

  const [viewYear, setViewYear] = useState(initial.year);
  const [viewMonthIndex0, setViewMonthIndex0] = useState(initial.monthIndex0);

  // data state
  const [availabilities, setAvailabilities] = useState<CoachAvailabilityResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);

  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // modal / form state
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [form, setForm] = useState<AvailabilityFormState>({
    startTime: "09:00",
    endTime: "17:00",
    recurring: false,
    repeatWeeks: 4,
  });

  // range for fetching (grid range is nicer than strict month)
  const range = useMemo(() => {
    return monthGridRangeISO(viewYear, viewMonthIndex0);
  }, [viewYear, viewMonthIndex0]);

  // fetch availabilities for visible range
  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await getMyCoachAvailabilities(range.fromISO, range.toISO);
      setAvailabilities(data);
    } catch (e: any) {
      console.error(e);
      setAvailabilities([]);
      setError(e?.message ?? "Failed to load availability.");
    } finally {
      setLoading(false);
    }
  }, [range.fromISO, range.toISO]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  // group by day for calendar dots / badges
  const daySummaryMap = useMemo(() => {
    const map = new Map<string, AvailabilityDaySummary>();

    for (const a of availabilities) {
      const key = a.date; // YYYY-MM-DD from backend
      const prev = map.get(key);
      if (!prev) {
        map.set(key, { date: key, hasAvailability: true, totalWindows: 1 });
      } else {
        map.set(key, {
          ...prev,
          totalWindows: (prev.totalWindows ?? 0) + 1,
        });
      }
    }

    return map;
  }, [availabilities]);

  const selectedDayAvailabilities = useMemo(() => {
    return availabilities
      .filter((a) => a.date === selectedDate)
      .sort((x, y) => x.startTime.localeCompare(y.startTime));
  }, [availabilities, selectedDate]);

  // month navigation
  const goPrevMonth = useCallback(() => {
    setSuccess(null);
    setError(null);
    setViewMonthIndex0((m) => {
      if (m === 0) {
        setViewYear((y) => y - 1);
        return 11;
      }
      return m - 1;
    });
  }, []);

  const goNextMonth = useCallback(() => {
    setSuccess(null);
    setError(null);
    setViewMonthIndex0((m) => {
      if (m === 11) {
        setViewYear((y) => y + 1);
        return 0;
      }
      return m + 1;
    });
  }, []);

  // open modal on day click (optional UX)
  const openForDate = useCallback((iso: string) => {
    setSelectedDate(iso);
    setIsModalOpen(true);
    setSuccess(null);
    setError(null);
  }, []);

  const closeModal = useCallback(() => setIsModalOpen(false), []);

  // create
  const createAvailabilityForSelectedDate = useCallback(async () => {
    setSuccess(null);
    setError(null);

    // basic client-side validation
    if (!isTimeStrValidHHmm(form.startTime) || !isTimeStrValidHHmm(form.endTime)) {
      setError("Please enter valid start/end times.");
      return;
    }

    if (timeToMinutes(form.endTime) <= timeToMinutes(form.startTime)) {
      setError("End time must be after start time.");
      return;
    }

    if (form.recurring) {
      const w = form.repeatWeeks ?? 0;
      if (w < 1 || w > 52) {
        setError("Repeat weeks must be between 1 and 52.");
        return;
      }
    }

    setCreating(true);
    try {
      await createCoachAvailability({
        date: selectedDate,
        startTime: form.startTime,
        endTime: form.endTime,
        recurring: form.recurring,
        repeatWeeks: form.recurring ? form.repeatWeeks : undefined,
      });

      setSuccess("Availability added.");
      await refresh();
      // optional: keep modal open for adding more, or close:
      // setIsModalOpen(false);
    } catch (e: any) {
      console.error(e);
      setError(e?.message ?? "Failed to create availability.");
    } finally {
      setCreating(false);
    }
  }, [form, refresh, selectedDate]);

  // delete
  const removeAvailability = useCallback(
    async (availabilityId: string) => {
      setSuccess(null);
      setError(null);

      try {
        await deleteMyCoachAvailability(availabilityId);
        setSuccess("Availability removed.");
        await refresh();
      } catch (e: any) {
        console.error(e);
        setError(e?.message ?? "Failed to remove availability.");
      }
    },
    [refresh]
  );

  // small helper label
  const viewLabel = useMemo(() => {
    const d = new Date(viewYear, viewMonthIndex0, 1);
    return d.toLocaleDateString(undefined, { year: "numeric", month: "long" });
  }, [viewYear, viewMonthIndex0]);

  return {
    // calendar view state
    viewYear,
    viewMonthIndex0,
    viewLabel,
    goPrevMonth,
    goNextMonth,

    // selection + modal
    selectedDate,
    setSelectedDate,
    openForDate,
    isModalOpen,
    closeModal,

    // data
    availabilities,
    selectedDayAvailabilities,
    daySummaryMap,
    loading,
    creating,
    error,
    success,

    // form
    form,
    setForm,

    // actions
    refresh,
    createAvailabilityForSelectedDate,
    removeAvailability,
  };
}
