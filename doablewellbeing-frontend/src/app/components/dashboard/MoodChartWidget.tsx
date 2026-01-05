"use client";

import { useState } from "react";
import type { DashboardWidgetView } from "@/types/dashboard";
import { useWidgetData } from "@/lib/hooks/useWidgetData";
import { useWidgetAction } from "@/lib/hooks/useWidgetAction";

type MoodChartWidgetData = {
  rangeDays: number;
  points: Array<{ at: string; score: number }>;
};

export function MoodChartWidget({ widget }: { widget: DashboardWidgetView }) {
  const { data, loading, error, reload } = useWidgetData<MoodChartWidgetData>(widget.id);
  const { busy, postJson } = useWidgetAction();
  const [score, setScore] = useState(6);
  const [note, setNote] = useState("");

  const points = data?.points ?? [];
  const rangeDays = data?.rangeDays ?? (widget.settings?.rangeDays ?? 14);

  async function logMood() {
    await postJson("/api/widgets/mood", { moodScore: score, note: note || null });
    setNote("");
    await reload();
  }

  if (loading) return <div className="text-sm text-gray-500">Loading…</div>;
  if (error) return <div className="text-sm text-red-600">{error}</div>;

  return (
    <div className="space-y-3">
      <div className="text-xs text-gray-500">Last {rangeDays} days</div>

      {points.length === 0 ? (
        <div className="rounded-xl border bg-gray-50 p-3 text-sm text-gray-600">
          No mood entries yet.
        </div>
      ) : (
        <div className="rounded-xl border bg-gray-50 p-2">
          <div className="flex h-[120px] items-end gap-1">
            {points.slice(-14).map((p, i) => (
              <div
                key={i}
                className="w-full rounded-sm bg-gray-400"
                style={{ height: `${(p.score / 10) * 100}%` }}
                title={`${new Date(p.at).toLocaleString()} → ${p.score}/10`}
              />
            ))}
          </div>
        </div>
      )}

      <div className="rounded-xl border p-3">
        <div className="text-sm font-semibold mb-2">Log mood</div>

        <div className="flex items-center gap-2">
          <input
            type="range"
            min={1}
            max={10}
            value={score}
            onChange={(e) => setScore(Number(e.target.value))}
            className="w-full"
          />
          <div className="w-10 text-right text-sm font-medium">{score}/10</div>
        </div>

        <textarea
          className="mt-2 w-full rounded-md border p-2 text-sm"
          rows={2}
          placeholder="Optional note…"
          value={note}
          onChange={(e) => setNote(e.target.value)}
        />

        <div className="mt-2 flex justify-end">
          <button
            className="rounded-md border px-3 py-2 text-sm disabled:opacity-50"
            disabled={busy}
            onClick={logMood}
          >
            {busy ? "Saving…" : "Save"}
          </button>
        </div>
      </div>
    </div>
  );
}
