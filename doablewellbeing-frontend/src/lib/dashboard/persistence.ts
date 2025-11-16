// lib/dashboard/persistence.ts
import type { Layout } from "react-grid-layout";
import { WidgetRegistry, type WidgetType, type WidgetInstance } from "@/app/components/widgets";
import { getDashboard, saveDashboard as saveDashboardApi, type DashboardApiDto, type LayoutsApiDto, type WidgetApiDto, type Breakpoint, } from "@/lib/api/dashboard";
export const breakpoints = { lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 };
export const cols         = { lg: 12,   md: 10,  sm: 8,  xs: 6,   xxs: 4 };
export type Layouts = Partial<Record<Breakpoint, Layout[]>>;

const DEFAULT_WIDGETS_API: WidgetApiDto[] = [
  { id: "habit-streak", type: "HabitStreak", title: "Habit streaks", settings: null },
];

export const DEFAULT_LAYOUTS: Layouts = {
  lg: [
    { i: "habit-streak", x: 0, y: 0, w: 4, h: 6, minW: 3, minH: 4 },
  ],
};

function isWidgetType(x: string): x is WidgetType {
  return x in WidgetRegistry;
}

function toInstance(w: WidgetApiDto): WidgetInstance | null {
  if (!w || typeof w.id !== "string" || typeof w.type !== "string") return null;
  if (!isWidgetType(w.type)) return null;
  return { id: w.id, type: w.type, title: w.title ?? undefined };
}

function fromInstance(w: WidgetInstance): WidgetApiDto {
  return {
    id: w.id,
    type: w.type,
    title: w.title ?? null,
    settings: null, 
}
}

const ALL_BPS: Breakpoint[] = ["lg", "md", "sm", "xs", "xxs"];

function parseLayouts(api: LayoutsApiDto | null | undefined): Layouts {
  const result: Layouts = {};

  if (!api) return result;

  for (const bp of ALL_BPS) {
    const raw = api[bp];
    if (!raw) continue;

    try {
      const arr = JSON.parse(raw) as Layout[];
      if (Array.isArray(arr)) {
        result[bp] = arr;
      }
    } catch {
    }
  }

  return result;
}

function stringifyLayouts(layouts: Layouts): LayoutsApiDto {
  const api: LayoutsApiDto = {
    lg: null,
    md: null,
    sm: null,
    xs: null,
    xxs: null,
  };

  for (const bp of ALL_BPS) {
    const arr = layouts[bp];
    if (arr && arr.length > 0) {
      api[bp] = JSON.stringify(arr);
    }
  }

  return api;
}


export interface DashboardState {
  widgets: WidgetInstance[];
  layouts: Layouts;
}


export async function loadDashboard(): Promise<DashboardState> {
  const fallbackWidgets = DEFAULT_WIDGETS_API
    .map(toInstance)
    .filter(Boolean) as WidgetInstance[];

  const fallbackLayouts = DEFAULT_LAYOUTS;

  try {
    const dto: DashboardApiDto = await getDashboard();

    const layoutsParsed = parseLayouts(dto.layouts);
    const layouts =
      Object.keys(layoutsParsed).length > 0 ? layoutsParsed : DEFAULT_LAYOUTS;

    const widgetsRaw =
      dto.widgets && dto.widgets.length > 0
        ? dto.widgets
        : DEFAULT_WIDGETS_API;

    const widgets = widgetsRaw
      .map(toInstance)
      .filter(Boolean) as WidgetInstance[];

    return { widgets, layouts };
  } catch {
    return { widgets: fallbackWidgets, layouts: fallbackLayouts };
  }
}

export async function saveDashboard(
  widgets: WidgetInstance[],
  layouts: Layouts,
): Promise<void> {
  const payload: DashboardApiDto = {
    name: "My dashboard", 
    layouts: stringifyLayouts(layouts),
    widgets: widgets.map(fromInstance),
  };

  try {
    await saveDashboardApi(payload);
  } catch (err) {
    if (process.env.NODE_ENV !== "production") {
     
      console.warn("Failed to save dashboard", err);
    }
  }
}
