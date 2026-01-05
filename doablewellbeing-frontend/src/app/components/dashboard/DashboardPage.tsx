"use client";

import React, { useMemo, useState } from "react";
import { useDashboard } from "@/lib/hooks/useDashboard";   
import { useModules } from "@/lib/hooks/useModules"; 
import { useDashboardEdit } from "@/lib/hooks/useDashboardEdit";
import { DashboardGrid } from "./DashboardGrid";  
import { AddWidgetModal } from "./AddwidgetModal";
import type { DashboardWidgetView } from "@/types/dashboard";
import { getDefaultWidgetSettings } from "./defaultWidgetSettings";

export function DashboardPage() {
  const { dashboard, setDashboard, loading, error, reload } = useDashboard();
  const { modules, loading: modulesLoading } = useModules();

  const { editMode, setEditMode, saving, addWidget, deleteWidget, updateSettings, savePlacements } =
    useDashboardEdit(dashboard, setDashboard);

  const [addOpen, setAddOpen] = useState(false);
  const [settingsWidget, setSettingsWidget] = useState<DashboardWidgetView | null>(null);

  const widgets = dashboard?.widgets ?? [];

  if (loading) return <div className="p-6">Loading dashboard…</div>;
  if (error) return (
    <div className="p-6">
      <div className="mb-3 text-red-600">{error}</div>
      <button className="rounded-md border px-3 py-2" onClick={reload}>Retry</button>
    </div>
  );

  if (!dashboard) return <div className="p-6">No dashboard.</div>;

  return (
    <div className="p-6 text-black">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <div className="text-2xl font-bold">Your Dashboard</div>
          <div className="text-sm text-gray-500">{dashboard.name}</div>
        </div>

        <div className="flex items-center gap-2">
          <button
            className="rounded-md border px-3 py-2 text-sm"
            onClick={() => setEditMode(!editMode)}
            disabled={saving}
          >
            {editMode ? "Done" : "Edit layout"}
          </button>

          <button
            className="rounded-md border px-3 py-2 text-sm"
            onClick={() => setAddOpen(true)}
            disabled={!editMode || saving || modulesLoading}
          >
            + Add widget
          </button>
        </div>
      </div>

      <DashboardGrid
        widgets={widgets}
        breakpoint="lg"
        editMode={editMode}
        saving={saving}
        onDeleteWidget={deleteWidget}
        onOpenSettings={(w) => setSettingsWidget(w)}
        onSavePlacements={savePlacements}
      />

      <AddWidgetModal
        open={addOpen}
        onClose={() => setAddOpen(false)}
        modules={modules}
        saving={saving}
       onAdd={async (moduleCode) => {
      await addWidget({
        moduleCode,
        title: undefined,
        settings: getDefaultWidgetSettings(moduleCode),
      });

    setAddOpen(false);
  }}
      />

      {/* Settings modal placeholder: most csak quick JSON edit */}
      {settingsWidget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
          <div className="w-full max-w-xl rounded-xl bg-white p-4 shadow-lg">
            <div className="mb-3 flex items-center justify-between">
              <div className="text-lg font-semibold">Widget settings</div>
              <button className="rounded-md border px-2 py-1 text-sm" onClick={() => setSettingsWidget(null)}>
                Close
              </button>
            </div>

            <div className="text-sm text-gray-600 mb-2">
              {settingsWidget.title || settingsWidget.moduleCode}
            </div>

            <textarea
              className="w-full rounded-md border p-3 font-mono text-xs"
              rows={10}
              defaultValue={JSON.stringify(settingsWidget.settings ?? {}, null, 2)}
              onBlur={async (e) => {
                try {
                  const parsed = JSON.parse(e.target.value);
                  await updateSettings(settingsWidget.id, parsed);
                } catch {
                  // ignore for now (later we can toast error)
                }
              }}
            />

            <div className="mt-3 text-xs text-gray-500">
              (Ide később csinálunk normális settings UI-t modulonként.)
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
