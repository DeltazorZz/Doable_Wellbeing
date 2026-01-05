"use client";

import { useEffect, useMemo, useState } from "react";
import {
  getCoachCalendar,
  confirmAppointment,
  declineAppointment,
  completeAppointment,
  cancelAppointment,
} from "@/lib/api/coachCalendarApi";
import { CoachCalendarEventView } from "@/app/components/coach/Overview/types";

// Local day helper: YYYY-MM-DD in user's locale timezone
function toLocalISODate(d: Date) {
  // sv-SE reliably returns YYYY-MM-DD
  return d.toLocaleDateString("sv-SE");
}
function isoToLocalDay(iso: string) {
  return toLocalISODate(new Date(iso));
}

function fmtTimeLabel(startsAt: string, endsAt: string) {
  const a = new Date(startsAt);
  const b = new Date(endsAt);
  const opts: Intl.DateTimeFormatOptions = { hour: "2-digit", minute: "2-digit" };
  return `${a.toLocaleTimeString(undefined, opts)} – ${b.toLocaleTimeString(undefined, opts)}`;
}

type Range = { fromIso: string; toIso: string };

export function useCoachCalendar() {
  // Selected day is for Agenda + "Today" stats
  const [selectedDay, setSelectedDay] = useState<string>(toLocalISODate(new Date()));

  // Range is for fetching the currently visible calendar range
  const [range, setRange] = useState<Range | null>(null);

  const [events, setEvents] = useState<CoachCalendarEventView[]>([]);
  const [loading, setLoading] = useState(false);

  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Used to force re-fetch after actions
  const [reloadKey, setReloadKey] = useState(0);

  // Fetch whenever range changes (FullCalendar controls this)
  useEffect(() => {
    if (!range) return;

    (async () => {
      setLoading(true);
      try {
        const data = await getCoachCalendar(range.fromIso, range.toIso);

        // Optional safety sort (helps when backend returns unsorted)
        const sorted = [...data].sort(
          (a, b) => +new Date(a.startsAt) - +new Date(b.startsAt)
        );

        setEvents(sorted);
      } finally {
        setLoading(false);
      }
    })();
  }, [range, reloadKey]);

  const uiEvents: CoachCalendarEventView[] = useMemo(() => {
    return events.map((e) => ({
      id: e.id,
      title: e.title ?? "Session",
      startsAt: e.startsAt,
      endsAt: e.endsAt,
      status: e.status,
      meetingUrl: e.meetingUrl ?? null,
      client: e.client ?? null,
      notesPreview: e.notesPreview ?? null,
      timeLabel: fmtTimeLabel(e.startsAt, e.endsAt),
    }));
  }, [events]);

  // FullCalendar format
  const calendarEvents = useMemo(() => {
    return uiEvents.map((e) => ({
      id: e.id,
      title: e.title,
      start: e.startsAt,
      end: e.endsAt,
      classNames: [
        "dw-event",
        e.status === "requested"
          ? "dw-event--requested"
          : e.status === "scheduled"
          ? "dw-event--scheduled"
          : e.status === "completed"
          ? "dw-event--completed"
          : "dw-event--negative",
      ],
    }));
  }, [uiEvents]);

  const dayAgenda = useMemo(() => {
    return uiEvents
      .filter((e) => isoToLocalDay(e.startsAt) === selectedDay)
      .sort((a, b) => +new Date(a.startsAt) - +new Date(b.startsAt));
  }, [uiEvents, selectedDay]);

  const selectedEvent = useMemo(() => {
    if (!selectedId) return null;
    return uiEvents.find((e) => e.id === selectedId) ?? null;
  }, [uiEvents, selectedId]);

  const stats = useMemo(() => {
    const requested = events.filter((e) => e.status === "requested").length;
    const today = toLocalISODate(new Date());
    const todayCount = events.filter((e) => isoToLocalDay(e.startsAt) === today).length;
    const week = events.length;
    return { requested, today: todayCount, week };
  }, [events]);

  async function reload() {
    setReloadKey((k) => k + 1);
  }

  async function doAction(fn: () => Promise<any>) {
    setActionLoading(true);
    try {
      await fn();
      await reload();
      setSelectedId(null);
    } finally {
      setActionLoading(false);
    }
  }

  const actions = {
    loading: actionLoading,
    confirm: (id: string) => doAction(() => confirmAppointment(id)),
    decline: (id: string, reason: string) => doAction(() => declineAppointment(id, reason)),
    complete: (id: string) => doAction(() => completeAppointment(id)),
    cancel: (id: string) => doAction(() => cancelAppointment(id)),
  };

  function goToday() {
    setSelectedDay(toLocalISODate(new Date()));
  }

  return {
    loading,
    stats,

    // calendar
    calendarEvents,
    range,
    setRange,

    // agenda / day selection
    selectedDay,
    setSelectedDay,
    dayAgenda,

    // drawer selection
    selectedEvent,
    openEvent: (id: string) => setSelectedId(id),
    closeEvent: () => setSelectedId(null),

    // actions
    actions,
    goToday,
  };
}
