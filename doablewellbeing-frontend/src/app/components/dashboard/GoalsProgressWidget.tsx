"use client";
import type { DashboardWidgetView } from "@/types/dashboard";

const FAKE = [
  { id: "g1", title: "Sleep routine", pct: 40 },
  { id: "g2", title: "Walk 2x/week", pct: 60 },
  { id: "g3", title: "Reduce caffeine", pct: 25 },
];

export function GoalsProgressWidget({ widget }: { widget: DashboardWidgetView }) {
  const max = Number(widget.settings?.showMax ?? 5);
  const goals = FAKE.slice(0, max);

  if (goals.length === 0) {
    return (
      <div className="flex h-full flex-col items-center justify-center text-center">
        <div className="mb-2 text-4xl">🎯</div>
        <div className="text-sm font-medium">No goals yet</div>
        <div className="mt-1 text-xs text-gray-500">
          Your coach can set milestones and track progress.
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {goals.map((g) => (
        <div key={g.id} className="rounded-xl border p-2">
          <div className="text-sm font-medium">{g.title}</div>
          <div className="mt-2 h-2 rounded-full bg-gray-200">
            <div className="h-2 rounded-full bg-gray-400" style={{ width: `${g.pct}%` }} />
          </div>
          <div className="mt-1 text-xs text-gray-500">{g.pct}%</div>
        </div>
      ))}
    </div>
  );
}
