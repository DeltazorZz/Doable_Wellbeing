"use client";

import type { DashboardWidgetView } from "@/types/dashboard";
import { useWidgetData } from "@/lib/hooks/useWidgetData";

type CompletedMeetingsWidgetData = {
  maxItems: number;
  sessions: Array<{
    id: string;
    dateLabel: string;
    title: string;
    coachSummary?: string | null;
    files: Array<{
      id: string;
      fileName: string;
      sizeLabel?: string | null;
      downloadUrl?: string | null;
    }>;
  }>;
};

export function CompletedMeetingsWidget({ widget }: { widget: DashboardWidgetView }) {
  const { data, loading, error, reload } = useWidgetData<CompletedMeetingsWidgetData>(widget.id);

  if (loading) return <div className="text-sm text-gray-500">Loading…</div>;
  if (error) return (
    <div className="text-sm">
      <div className="text-red-600 mb-2">{error}</div>
      <button className="rounded-md border px-2 py-1 text-xs" onClick={reload}>Retry</button>
    </div>
  );

  const sessions = data?.sessions ?? [];
  if (sessions.length === 0) {
    return (
      <div className="flex h-full flex-col items-center justify-center text-center">
        <div className="mb-2 text-4xl">📁</div>
        <div className="text-sm font-medium">No completed meetings yet</div>
        <div className="mt-1 text-xs text-gray-500">
          Your coach’s notes and files will appear here.
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {sessions.map((s) => (
        <div key={s.id} className="rounded-xl border p-3">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <div className="truncate text-sm font-semibold">{s.title}</div>
              <div className="text-xs text-gray-500">{s.dateLabel}</div>
            </div>
          </div>

          <div className="mt-2 text-sm text-gray-700">
            {s.coachSummary ? (
              <p className="whitespace-pre-wrap">{s.coachSummary}</p>
            ) : (
              <p className="text-xs text-gray-500 italic">No coach summary.</p>
            )}
          </div>

          <div className="mt-3">
            <div className="mb-1 text-xs font-medium text-gray-700">Resources</div>

            {s.files.length === 0 ? (
              <div className="text-xs text-gray-500 italic">No files attached.</div>
            ) : (
              <div className="space-y-2">
                {s.files.map((f) => (
                  <div key={f.id} className="flex items-center justify-between rounded-lg border p-2">
                    <div className="min-w-0">
                      <div className="truncate text-xs font-medium">{f.fileName}</div>
                      {f.sizeLabel && <div className="text-[11px] text-gray-500">{f.sizeLabel}</div>}
                    </div>

                    {f.downloadUrl ? (
                      <a
                        className="rounded-md border px-2 py-1 text-xs"
                        href={f.downloadUrl}
                        target="_blank"
                        rel="noreferrer"
                      >
                        Download
                      </a>
                    ) : (
                      <button className="rounded-md border px-2 py-1 text-xs disabled:opacity-50" disabled>
                        Download
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
