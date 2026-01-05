"use client";
import type { DashboardWidgetView } from "@/types/dashboard";

const FAKE = [
  "Take 3 intentional, deep breaths",
  "Name 3 things you can see/hear/feel",
  "Write one sentence in a journal",
  "Put one single thing away",
  "Open a window for 5 minutes",
];

export function MicroHabitsWidget({ widget }: { widget: DashboardWidgetView }) {
  const max = Number(widget.settings?.showMax ?? 5);
  const items = FAKE.slice(0, max);

  if (items.length === 0) {
    return (
      <div className="flex h-full flex-col items-center justify-center text-center">
        <div className="mb-2 text-4xl">🌱</div>
        <div className="text-sm font-medium">No micro-habits yet</div>
        <div className="mt-1 text-xs text-gray-500">
          You’ll get 1–2 tiny actions tailored to you.
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {items.map((t, idx) => (
        <div key={idx} className="rounded-lg border p-2">
          <div className="text-sm">{t}</div>
          <div className="mt-2 flex justify-end">
            <button className="rounded-md border px-2 py-1 text-xs disabled:opacity-50" disabled>
              Add to habits
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
