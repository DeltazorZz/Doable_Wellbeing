"use client";
import type { DashboardWidgetView } from "@/types/dashboard";

const AREAS = [
  { key: "health", label: "Health", score: 6 },
  { key: "relationships", label: "Relationships", score: 7 },
  { key: "work", label: "Work", score: 5 },
  { key: "money", label: "Money", score: 4 },
  { key: "fun", label: "Fun", score: 6 },
  { key: "growth", label: "Growth", score: 7 },
  { key: "home", label: "Home", score: 5 },
  { key: "mind", label: "Mind", score: 6 },
];

export function WheelOfLifeWidget({ widget }: { widget: DashboardWidgetView }) {
  const showLabels = Boolean(widget.settings?.showLabels ?? true);

  return (
    <div className="space-y-2">
      <div className="text-xs text-gray-500">
        Score areas from 1–10. We’ll track improvement per session.
      </div>

      <div className="grid grid-cols-2 gap-2">
        {AREAS.map((a) => (
          <div key={a.key} className="rounded-xl border p-2">
            {showLabels && <div className="text-xs font-medium">{a.label}</div>}
            <div className="mt-1 flex items-center gap-2">
              <div className="h-2 flex-1 rounded-full bg-gray-200">
                <div
                  className="h-2 rounded-full bg-gray-400"
                  style={{ width: `${(a.score / 10) * 100}%` }}
                />
              </div>
              <div className="w-7 text-right text-xs text-gray-600">{a.score}/10</div>
            </div>
          </div>
        ))}
      </div>

      <button className="rounded-md border px-2 py-1 text-xs disabled:opacity-50" disabled>
        Update scores
      </button>
    </div>
  );
}
