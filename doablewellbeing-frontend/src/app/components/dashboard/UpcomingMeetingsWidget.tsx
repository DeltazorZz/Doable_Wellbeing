"use client";

import type { DashboardWidgetView } from "@/types/dashboard";
import { useWidgetData } from "@/lib/hooks/useWidgetData";

type UpcomingMeetingsWidgetData = {
  showDaysAhead: number;
  meetings: Array<{
    id: string;
    title: string;
    startsAt: string;
    endsAt: string;
    status: string;
    meetingUrl?: string | null;
  }>;
};

export function UpcomingMeetingsWidget({ widget }: { widget: DashboardWidgetView }) {
  const { data, loading, error, reload } = useWidgetData<UpcomingMeetingsWidgetData>(widget.id);

  if (loading) return <div className="text-sm text-gray-500">Loading…</div>;
  if (error) return (
    <div className="text-sm">
      <div className="text-red-600 mb-2">{error}</div>
      <button className="rounded-md border px-2 py-1 text-xs" onClick={reload}>Retry</button>
    </div>
  );

  const meetings = data?.meetings ?? [];
  const days = data?.showDaysAhead ?? (widget.settings?.showDaysAhead ?? 14);

  if (meetings.length === 0) {
    return (
      <div className="flex h-full flex-col items-center justify-center text-center">
        <div className="mb-2 text-4xl">📅</div>
        <div className="text-sm font-medium">No upcoming meetings</div>
        <div className="mt-1 text-xs text-gray-500">
          Showing next {days} days.
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <div className="text-xs text-gray-500">Next {days} days</div>

      {meetings.map((m) => (
        <div key={m.id} className="rounded-xl border p-2">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <div className="truncate text-sm font-semibold">{m.title}</div>
              <div className="text-xs text-gray-500">
                {new Date(m.startsAt).toLocaleString()} – {new Date(m.endsAt).toLocaleTimeString()}
              </div>
              <div className="mt-1 inline-block rounded-full border px-2 py-0.5 text-[11px] text-gray-600">
                {m.status}
              </div>
            </div>

            {m.meetingUrl ? (
              <a
                className="rounded-md border px-2 py-1 text-xs"
                href={m.meetingUrl}
                target="_blank"
                rel="noreferrer"
              >
                Join
              </a>
            ) : (
              <button className="rounded-md border px-2 py-1 text-xs disabled:opacity-50" disabled>
                No link
              </button>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
