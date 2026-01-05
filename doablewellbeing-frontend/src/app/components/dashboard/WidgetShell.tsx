"use client";

import React from "react";
import type { DashboardWidgetView } from "@/types/dashboard";
import { WidgetBody } from "./WidgetRegistry";

export function WidgetShell({
  widget,
  editMode,
  saving,
  onDelete,
  onOpenSettings,
}: {
  widget: DashboardWidgetView;
  editMode: boolean;
  saving: boolean;
  onDelete: (id: string) => void;
  onOpenSettings: (w: DashboardWidgetView) => void;
}) {
  return (
    <div className="h-full w-full rounded-xl border bg-white p-3 shadow-sm">
      <div className="mb-2 flex items-center justify-between gap-2">
        <div className="min-w-0">
          <div className="truncate font-semibold">
            {widget.title || widget.moduleCode}
          </div>
          <div className="text-xs text-gray-500">{widget.moduleCode}</div>
        </div>

        {editMode && (
          <div className="flex gap-2">
            <button
              className="rounded-md border px-2 py-1 text-xs"
              onClick={() => onOpenSettings(widget)}
              disabled={saving}
            >
              Settings
            </button>
            <button
              className="rounded-md border px-2 py-1 text-xs text-red-600"
              onClick={() => onDelete(widget.id)}
              disabled={saving}
            >
              Remove
            </button>
          </div>
        )}
      </div>

      <div className="text-sm text-gray-800">
        <WidgetBody widget={widget} />
      </div>
    </div>
  );
}
