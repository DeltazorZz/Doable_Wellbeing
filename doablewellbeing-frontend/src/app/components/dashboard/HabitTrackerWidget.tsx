"use client";

import { useState } from "react";
import type { DashboardWidgetView } from "@/types/dashboard";
import { useWidgetData } from "@/lib/hooks/useWidgetData";
import { useWidgetAction } from "@/lib/hooks/useWidgetAction";

type HabitTrackerWidgetData = {
  showMax: number;
  habits: Array<{
    id: string;
    title: string;
    doneToday: boolean;
    streak: number;
  }>;
};

export function HabitTrackerWidget({ widget }: { widget: DashboardWidgetView }) {
  const { data, loading, error, reload } = useWidgetData<HabitTrackerWidgetData>(widget.id);
  const { busy, postJson, postNoBody } = useWidgetAction();
  const [newTitle, setNewTitle] = useState("");

  async function createHabit() {
    const title = newTitle.trim();
    if (!title) return;
    await postJson("/api/widgets/habits", { title });
    setNewTitle("");
    await reload();
  }

  async function markDone(habitId: string) {
    await postNoBody(`/api/habits/${habitId}/done`);
    await reload();
  }

  if (loading) return <div className="text-sm text-gray-500">Loading…</div>;
  if (error) return <div className="text-sm text-red-600">{error}</div>;

  const habits = data?.habits ?? [];

  return (
    <div className="space-y-3">
      <div className="rounded-xl border p-3">
        <div className="text-sm font-semibold mb-2">Add habit</div>
        <div className="flex gap-2">
          <input
            className="w-full rounded-md border px-3 py-2 text-sm"
            placeholder="e.g., Drink water"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
          />
          <button
            className="rounded-md border px-3 py-2 text-sm disabled:opacity-50"
            disabled={busy}
            onClick={createHabit}
          >
            Add
          </button>
        </div>
      </div>

      {habits.length === 0 ? (
        <div className="flex h-full flex-col items-center justify-center text-center">
          <div className="mb-2 text-4xl">✅</div>
          <div className="text-sm font-medium">No habits yet</div>
          <div className="mt-1 text-xs text-gray-500">
            Add a small habit and track your streak.
          </div>
        </div>
      ) : (
        <div className="space-y-2">
          {habits.map((h) => (
            <div key={h.id} className="flex items-center justify-between rounded-xl border p-2">
              <div className="min-w-0">
                <div className="truncate text-sm font-semibold">{h.title}</div>
                <div className="text-xs text-gray-500">Streak: {h.streak} day(s)</div>
              </div>

              <button
                className="rounded-md border px-3 py-2 text-xs disabled:opacity-50"
                disabled={busy || h.doneToday}
                onClick={() => markDone(h.id)}
                title={h.doneToday ? "Already done today" : "Mark done"}
              >
                {h.doneToday ? "Done" : "Mark done"}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
