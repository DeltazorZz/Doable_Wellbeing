"use client";

import { Coach } from "@/lib/api/coachApi";

interface CoachSelectProps {
  coaches: Coach[];
  selectedCoach: string;
  onChange: (id: string) => void;
  loading?: boolean;
}

export function CoachSelect({
  coaches,
  selectedCoach,
  onChange,
  loading = false,
}: CoachSelectProps) {
  const isEmpty = coaches.length === 0;

  return (
    <div>
      <label
        htmlFor="coach"
        className="mb-2 block text-sm font-medium text-gray-900"
      >
        With (Coach)
      </label>

      <div className="relative w-full">
        <select
          id="coach"
          value={selectedCoach}
          onChange={(e) => onChange(e.target.value)}
          className="w-full appearance-none rounded-xl border border-gray-300 bg-white px-4 py-3 pr-10 text-gray-900 shadow-sm outline-none ring-0 transition focus:border-gray-900"
          disabled={loading || isEmpty}
        >
          {/* Loading állapot */}
          {loading && <option>Loading coaches…</option>}

          {/* Ha nincs coach */}
          {!loading && isEmpty && <option>No coaches available</option>}

          {/* Coach lista */}
          {!loading &&
            coaches.map((c) => (
              <option key={c.id} value={c.id}>
                {c.displayName}
              </option>
            ))}
        </select>

        <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
          ▾
        </span>
      </div>
    </div>
  );
}
