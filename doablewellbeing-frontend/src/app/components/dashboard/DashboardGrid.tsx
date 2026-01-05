"use client";

import React, { useEffect, useMemo, useRef } from "react";
import type { Breakpoint, DashboardWidgetView, PlacementView } from "@/types/dashboard";
import { WidgetShell } from "./WidgetShell";

// Gridstack import (NPM: gridstack)
import { GridStack } from "gridstack";
import "gridstack/dist/gridstack.min.css";

function pickPlacement(widget: DashboardWidgetView, bp: Breakpoint): PlacementView {
  // fallback chain: bp -> lg -> first found -> default
  return (
    widget.placements?.[bp] ??
    widget.placements?.lg ??
    Object.values(widget.placements ?? {})[0] ??
    { x: 0, y: 0, w: 6, h: 4 }
  );
}

export function DashboardGrid({
  widgets,
  breakpoint = "lg",
  editMode,
  saving,
  onDeleteWidget,
  onOpenSettings,
  onSavePlacements,
}: {
  widgets: DashboardWidgetView[];
  breakpoint?: Breakpoint;
  editMode: boolean;
  saving: boolean;
  onDeleteWidget: (id: string) => void;
  onOpenSettings: (w: DashboardWidgetView) => void;
  onSavePlacements: (bp: Breakpoint, placements: Array<{ widgetId: string } & PlacementView>) => void;
}) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const gridRef = useRef<GridStack | null>(null);

  // Precompute placements for current breakpoint
  const items = useMemo(() => {
    return widgets
      .filter(w => w.isActive)
      .map(w => {
        const p = pickPlacement(w, breakpoint);
        return { widget: w, p };
      });
  }, [widgets, breakpoint]);

  useEffect(() => {
    if (!containerRef.current) return;

    // destroy old grid (re-init on widgets changes is simplest + stable)
    if (gridRef.current) {
      gridRef.current.destroy(false);
      gridRef.current = null;
    }

    const grid = GridStack.init(
      {
        float: true,
        margin: 8,
        cellHeight: 90,
        draggable: { handle: ".gridstack-handle" }, // optional if you add handle
        resizable: { handles: "e, se, s, sw, w" },
        disableDrag: !editMode,
        disableResize: !editMode,
      },
      containerRef.current
    );

    gridRef.current = grid;

    // events -> collect layout and save
    const onChange = () => {
      // only save in edit mode
      if (!editMode) return;

      const nodes = grid.engine.nodes;
      const placements = nodes.map(n => ({
        widgetId: String(n.id),
        x: n.x ?? 0,
        y: n.y ?? 0,
        w: n.w ?? 1,
        h: n.h ?? 1,
      }));

      onSavePlacements(breakpoint, placements);
    };

    // debounce: Gridstack can spam events; simple timeout
    let t: any = null;
    const debounced = () => {
      if (t) clearTimeout(t);
      t = setTimeout(onChange, 350);
    };

    grid.on("change", debounced);

    return () => {
      if (t) clearTimeout(t);
      (grid as any).off("change", debounced);
      grid.destroy(false);
      gridRef.current = null;
    };
  }, [items, editMode, breakpoint, onSavePlacements]);

  return (
    <div className="grid-stack" ref={containerRef}>
      {items.map(({ widget, p }) => (
        <div
          key={widget.id}
          className="grid-stack-item"
          gs-id={widget.id}
          gs-x={p.x}
          gs-y={p.y}
          gs-w={p.w}
          gs-h={p.h}
        >
          <div className="grid-stack-item-content">
            <WidgetShell
              widget={widget}
              editMode={editMode}
              saving={saving}
              onDelete={onDeleteWidget}
              onOpenSettings={onOpenSettings}
            />
          </div>
        </div>
      ))}
    </div>
  );
}
