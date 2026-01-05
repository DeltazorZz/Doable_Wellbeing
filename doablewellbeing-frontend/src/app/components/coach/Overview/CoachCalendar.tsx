"use client";

import React, { useMemo } from "react";
import FullCalendar from "@fullcalendar/react";
import timeGridPlugin from "@fullcalendar/timegrid";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin, { DateClickArg } from "@fullcalendar/interaction";

import { useCoachCalendar } from "@/lib/hooks/useCoachCalendar";
import { AgendaList } from "./AgendaList";
import { SessionDrawer } from "./SessionDrawer";

function toLocalISODate(d: Date) {
  return d.toLocaleDateString("sv-SE");
}

export function CoachCalendar() {
  const {
    loading,
    stats,
    calendarEvents,

    // NEW: range comes from calendar visible dates
    setRange,

    // NEW: selectedDay is the agenda "current day"
    selectedDay,
    setSelectedDay,

    dayAgenda,
    selectedEvent,
    openEvent,
    closeEvent,
    actions,
    goToday,
  } = useCoachCalendar();

  const subtitle = useMemo(() => {
    const d = new Date(selectedDay + "T00:00:00");
    return d.toLocaleDateString(undefined, {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }, [selectedDay]);

  return (
    <div className="grid gap-4 lg:grid-cols-[320px_minmax(0,1fr)]">
      {/* LEFT COLUMN */}
      <div className="space-y-4">
        <aside className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
          <div className="mb-3">
            <div className="text-sm font-semibold text-slate-900">Coach calendar</div>
            <div className="text-xs text-slate-500">Manage sessions like a pro.</div>
          </div>

          <div className="space-y-2">
            <QuickStat label="Requests" value={stats.requested} />
            <QuickStat label="Today" value={stats.today} />
            <QuickStat label="This week" value={stats.week} />
          </div>

          <div className="mt-4 flex gap-2">
            <button
              type="button"
              onClick={goToday}
              className="flex-1 rounded-xl bg-slate-900 px-3 py-2 text-xs font-semibold text-white hover:bg-black"
            >
              Today
            </button>
            <button
              type="button"
              onClick={() => setSelectedDay(toLocalISODate(new Date()))}
              className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-800 hover:bg-slate-50"
            >
              Jump
            </button>
          </div>

          <div className="mt-4 rounded-xl border border-slate-200 bg-slate-50 p-3 text-xs text-slate-600">
            Click a session in the calendar → opens actions in a modal.
          </div>

          <div className="mt-4">
            <div className="mb-2 text-xs font-semibold text-slate-700">Legend</div>
            <div className="space-y-1 text-xs text-slate-600">
              <LegendRow label="Requested" dotClass="bg-amber-500" />
              <LegendRow label="Scheduled" dotClass="bg-emerald-600" />
              <LegendRow label="Completed" dotClass="bg-slate-500" />
              <LegendRow label="Declined/Cancelled" dotClass="bg-rose-500" />
            </div>
          </div>
        </aside>

        <section className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
          <div className="mb-3 flex items-center justify-between">
            <div>
              <div className="text-sm font-semibold text-slate-900">Agenda</div>
              <div className="text-xs text-slate-500">Sessions for selected day</div>
            </div>
            <span className="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-700">
              {dayAgenda.length} items
            </span>
          </div>

          <AgendaList
            events={dayAgenda}
            selectedId={selectedEvent?.id ?? null}
            onSelect={openEvent}
          />
        </section>
      </div>

      {/* RIGHT COLUMN */}
      <article className="space-y-4 min-w-0">
        <div className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
          <div className="mb-3 flex items-start justify-between gap-3">
            <div>
              <div className="text-lg font-semibold text-slate-900">Calendar</div>
              <div className="text-sm text-slate-500">{subtitle}</div>
            </div>

            {loading ? (
              <span className="text-xs text-slate-500">Loading…</span>
            ) : (
              <span className="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-700">
                {calendarEvents.length} sessions loaded
              </span>
            )}
          </div>

          <div className="rounded-xl border border-slate-200 p-2">
            <FullCalendar
              plugins={[timeGridPlugin, dayGridPlugin, interactionPlugin]}
              initialView="timeGridWeek"
              headerToolbar={{
                left: "prev,next",
                center: "title",
                right: "timeGridDay,timeGridWeek,dayGridMonth",
              }}
              height="auto"
              nowIndicator
              selectable={false}
              events={calendarEvents}

              // IMPORTANT: align calendar to local timezone + Monday start
              timeZone="local"
              firstDay={1}

              // This drives backend fetch range (NOT selectedDay)
              datesSet={(arg) => {
                setRange({ fromIso: arg.start.toISOString(), toIso: arg.end.toISOString() });
              }}

              // Clicking a day updates agenda selected day
              dateClick={(arg: DateClickArg) => {
                setSelectedDay(toLocalISODate(arg.date));
              }}

              eventClick={(info) => {
                info.jsEvent.preventDefault();
                openEvent(String(info.event.id));
              }}

              eventContent={(arg) => (
                <div className="px-1">
                  <div className="text-[11px] font-semibold leading-tight">{arg.timeText}</div>
                  <div className="text-[11px] leading-tight opacity-90">{arg.event.title}</div>
                </div>
              )}
            />
          </div>
        </div>
      </article>

      <SessionDrawer
        open={!!selectedEvent}
        event={selectedEvent}
        onClose={closeEvent}
        loading={actions.loading}
        onConfirm={actions.confirm}
        onDecline={actions.decline}
        onComplete={actions.complete}
        onCancel={actions.cancel}
      />
    </div>
  );
}

function QuickStat({ label, value }: { label: string; value: number }) {
  return (
    <div className="flex items-center justify-between rounded-xl border border-slate-200 bg-white px-3 py-2">
      <div className="text-xs font-medium text-slate-600">{label}</div>
      <div className="text-sm font-semibold text-slate-900">{value}</div>
    </div>
  );
}

function LegendRow({ label, dotClass }: { label: string; dotClass: string }) {
  return (
    <div className="flex items-center gap-2">
      <span className={"h-2 w-2 rounded-full " + dotClass} />
      <span>{label}</span>
    </div>
  );
}
