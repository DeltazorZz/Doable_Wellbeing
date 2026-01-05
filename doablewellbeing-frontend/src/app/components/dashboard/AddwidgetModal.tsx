"use client";

import React, { useState } from "react";
import type { ModuleView } from "@/types/dashboard";

export function AddWidgetModal({
  open,
  onClose,
  modules,
  onAdd,
  saving,
}: {
  open: boolean;
  onClose: () => void;
  modules: ModuleView[];
  onAdd: (moduleCode: string) => void;
  saving: boolean;
}) {
  const [filter, setFilter] = useState("");

  if (!open) return null;

  const filtered = modules.filter(m =>
    (m.name + " " + m.code).toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
      <div className="w-full max-w-xl rounded-xl bg-white p-4 shadow-lg">
        <div className="mb-3 flex items-center justify-between">
          <div className="text-lg font-semibold">Add widget</div>
          <button className="rounded-md border px-2 py-1 text-sm" onClick={onClose}>
            Close
          </button>
        </div>

        <input
          className="mb-3 w-full rounded-md border px-3 py-2 text-sm"
          placeholder="Search modules…"
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
        />

        <div className="max-h-[360px] overflow-auto space-y-2">
          {filtered.map(m => (
            <button
              key={m.code}
              className="w-full rounded-lg border p-3 text-left hover:bg-gray-50 disabled:opacity-50"
              disabled={saving}
              onClick={() => onAdd(m.code)}
            >
              <div className="font-medium">{m.name}</div>
              <div className="text-xs text-gray-500">{m.code}</div>
              {m.description && <div className="mt-1 text-sm text-gray-700">{m.description}</div>}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
