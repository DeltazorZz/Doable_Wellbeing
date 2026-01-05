"use client";

import { useCallback, useState } from "react";
import type { Breakpoint, DashboardView, DashboardWidgetView, PlacementView } from "@/types/dashboard";
import {
  addWidget as apiAddWidget,
  deleteWidget as apiDeleteWidget,
  updateWidgetSettings as apiUpdateSettings,
  updatePlacements as apiUpdatePlacements,
} from "@/lib/api/dashboardApi";

export function useDashboardEdit(
  dashboard: DashboardView | null,
  setDashboard: (d: DashboardView | null) => void
) {
  const [editMode, setEditMode] = useState(false);
  const [saving, setSaving] = useState(false);

  const addWidget = useCallback(async (payload: { moduleCode: string; title?: string; settings?: any }) => {
    if (!dashboard) return;
    setSaving(true);
    try {
      const { widgetId } = await apiAddWidget(dashboard.dashboardId, {
        moduleCode: payload.moduleCode,
        title: payload.title ?? null,
        settings: payload.settings ?? {},
        breakpoint: "lg",
        x: 0, y: 0, w: 6, h: 4,
      });

      // optimistic: add to local state (placements will be returned after reload too)
      const newWidget: DashboardWidgetView = {
        id: widgetId,
        moduleCode: payload.moduleCode,
        title: payload.title ?? null,
        settings: payload.settings ?? {},
        isActive: true,
        placements: { lg: { x: 0, y: 0, w: 6, h: 4 } },
      };

      setDashboard({
        ...dashboard,
        widgets: [...dashboard.widgets, newWidget],
      });
    } finally {
      setSaving(false);
    }
  }, [dashboard, setDashboard]);

  const deleteWidget = useCallback(async (widgetId: string) => {
    if (!dashboard) return;
    setSaving(true);
    try {
      await apiDeleteWidget(dashboard.dashboardId, widgetId);
      setDashboard({
        ...dashboard,
        widgets: dashboard.widgets.filter(w => w.id !== widgetId),
      });
    } finally {
      setSaving(false);
    }
  }, [dashboard, setDashboard]);

  const updateSettings = useCallback(async (widgetId: string, settings: any) => {
    if (!dashboard) return;
    setSaving(true);
    try {
      await apiUpdateSettings(dashboard.dashboardId, widgetId, settings);
      setDashboard({
        ...dashboard,
        widgets: dashboard.widgets.map(w => (w.id === widgetId ? { ...w, settings } : w)),
      });
    } finally {
      setSaving(false);
    }
  }, [dashboard, setDashboard]);

  const savePlacements = useCallback(async (breakpoint: Breakpoint, placements: Array<{ widgetId: string } & PlacementView>) => {
    if (!dashboard) return;
    setSaving(true);
    try {
      await apiUpdatePlacements(
        dashboard.dashboardId,
        placements.map(p => ({
          widgetId: p.widgetId,
          breakpoint,
          x: p.x, y: p.y, w: p.w, h: p.h,
          minW: p.minW ?? null,
          minH: p.minH ?? null,
          maxW: p.maxW ?? null,
          maxH: p.maxH ?? null,
          isStatic: p.isStatic ?? null,
        }))
      );

      // update local state placements
      setDashboard({
        ...dashboard,
        widgets: dashboard.widgets.map(w => {
          const found = placements.find(p => p.widgetId === w.id);
          if (!found) return w;
          return {
            ...w,
            placements: {
              ...w.placements,
              [breakpoint]: {
                x: found.x, y: found.y, w: found.w, h: found.h,
                minW: found.minW ?? null,
                minH: found.minH ?? null,
                maxW: found.maxW ?? null,
                maxH: found.maxH ?? null,
                isStatic: found.isStatic ?? null,
              },
            },
          };
        }),
      });
    } finally {
      setSaving(false);
    }
  }, [dashboard, setDashboard]);

  return { editMode, setEditMode, saving, addWidget, deleteWidget, updateSettings, savePlacements };
}
