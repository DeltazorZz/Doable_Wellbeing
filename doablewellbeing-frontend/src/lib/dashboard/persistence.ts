// src/lib/dashboard/persistence.ts
import type { Layout } from "react-grid-layout";
import type { Breakpoint, DashboardView, PlacementView } from "@/types/dashboard";

export const breakpoints = { lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 };
export const cols        = { lg: 12,   md: 10,  sm: 8,  xs: 6,   xxs: 4 };

export type Layouts = Partial<Record<Breakpoint, Layout[]>>;

const ALL_BPS: Breakpoint[] = ["lg", "md", "sm", "xs", "xxs"];

function placementToLayout(widgetId: string, p: PlacementView): Layout {
  return {
    i: widgetId,
    x: p.x,
    y: p.y,
    w: p.w,
    h: p.h,
    minW: p.minW ?? undefined,
    minH: p.minH ?? undefined,
    maxW: p.maxW ?? undefined,
    maxH: p.maxH ?? undefined,
    static: p.isStatic ?? undefined,
  };
}

export function dashboardToLayouts(dashboard: DashboardView): Layouts {
  const result: Layouts = {};

  for (const bp of ALL_BPS) {
    const layoutsForBp: Layout[] = [];

    for (const w of dashboard.widgets) {
      const p = w.placements?.[bp];
      if (!p) continue;
      layoutsForBp.push(placementToLayout(w.id, p));
    }

    if (layoutsForBp.length > 0) result[bp] = layoutsForBp;
  }

  return result;
}

export function layoutsToPlacementUpdates(
  breakpoint: Breakpoint,
  layouts: Layout[]
): Array<{ widgetId: string } & PlacementView> {
  return layouts.map(l => ({
    widgetId: l.i,
    x: l.x ?? 0,
    y: l.y ?? 0,
    w: l.w ?? 1,
    h: l.h ?? 1,
    minW: l.minW ?? null,
    minH: l.minH ?? null,
    maxW: (l as any).maxW ?? null,
    maxH: (l as any).maxH ?? null,
    isStatic: (l as any).static ?? null,
  }));
}
