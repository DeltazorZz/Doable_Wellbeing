"use client";

import React from "react";
import { CoachCalendarEventView } from "./types";

export function AgendaList({
  events,
  selectedId,
  onSelect,
}: {
  events: CoachCalendarEventView[];
  selectedId: string | null;
  onSelect: (id: string) => void;
}) {
  if (events.length === 0) {
    return (
      <div className="rounded-2xl border border-dashed border-slate-200 p-10 text-center">
        <div className="text-sm font-medium text-slate-800">No sessions</div>
        <div className="mt-1 text-xs text-slate-500">This day is free. Nice.</div>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {events.map((e) => {
        const picked = e.id === selectedId;

        return (
          <button
            key={e.id}
            type="button"
            onClick={() => onSelect(e.id)}
            className={
              "w-full rounded-2xl border px-4 py-3 text-left transition " +
              (picked
                ? "border-slate-900 bg-slate-900 text-white"
                : "border-slate-200 bg-white hover:border-slate-400")
            }
          >
            <div className="flex items-start justify-between gap-3">
              <div>
                <div className={"text-sm font-semibold " + (picked ? "text-white" : "text-slate-900")}>
                  {e.title}
                </div>
                <div className={"mt-0.5 text-xs " + (picked ? "text-slate-200" : "text-slate-500")}>
                  {e.startsAt + ' - ' + e.endsAt } • {e.client?.email ?? "—"}
                </div>

              </div>

              <StatusPill status={e.status} picked={picked} />
            </div>

            {e.notesPreview && (
              <div className={"mt-2 line-clamp-2 text-xs " + (picked ? "text-slate-200" : "text-slate-600")}>
                {e.notesPreview}
              </div>
            )}
          </button>
        );
      })}
    </div>
  );
}

function StatusPill({ status, picked }: { status: string; picked: boolean }) {
  const base =
    "inline-flex items-center rounded-full px-2.5 py-1 text-[11px] font-medium ring-1 ring-inset";

  const map: Record<string, string> = {
    requested: picked
      ? "bg-white/10 text-white ring-white/20"
      : "bg-amber-50 text-amber-700 ring-amber-200",
    scheduled: picked
      ? "bg-white/10 text-white ring-white/20"
      : "bg-emerald-50 text-emerald-700 ring-emerald-200",
    completed: picked
      ? "bg-white/10 text-white ring-white/20"
      : "bg-slate-100 text-slate-700 ring-slate-200",
    declined: picked
      ? "bg-white/10 text-white ring-white/20"
      : "bg-rose-50 text-rose-700 ring-rose-200",
    cancelled: picked
      ? "bg-white/10 text-white ring-white/20"
      : "bg-slate-100 text-slate-600 ring-slate-200",
  };

  return <span className={`${base} ${map[status] ?? map.scheduled}`}>{status}</span>;
}
