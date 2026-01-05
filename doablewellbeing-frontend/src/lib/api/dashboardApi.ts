import { apiFetch } from "./apiFetch";
import type { DashboardView, ModuleView, Breakpoint } from "@/types/dashboard";

export async function fetchDefaultDashboard(): Promise<DashboardView> {
  return apiFetch<DashboardView>("/api/dashboards/default");
}

export async function fetchModules(): Promise<ModuleView[]> {
  return apiFetch<ModuleView[]>("/api/modules");
}

export async function addWidget(
  dashboardId: string,
  payload: {
    moduleCode: string;
    title?: string | null;
    settings?: any;
    x?: number; y?: number; w?: number; h?: number;
    breakpoint?: Breakpoint;
  }
): Promise<{ widgetId: string }> {
  return apiFetch<{ widgetId: string }>(`/api/dashboards/${dashboardId}/widgets`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function updatePlacements(
  dashboardId: string,
  placements: Array<{
    widgetId: string;
    breakpoint: Breakpoint;
    x: number; y: number; w: number; h: number;
    minW?: number | null; minH?: number | null;
    maxW?: number | null; maxH?: number | null;
    isStatic?: boolean | null;
  }>
): Promise<{ status: string }> {
  return apiFetch<{ status: string }>(`/api/dashboards/${dashboardId}/placements`, {
    method: "PUT",
    body: JSON.stringify({ placements }),
  });
}

export async function updateWidgetSettings(
  dashboardId: string,
  widgetId: string,
  settings: any
): Promise<{ status: string }> {
  return apiFetch<{ status: string }>(`/api/dashboards/${dashboardId}/widgets/${widgetId}/settings`, {
    method: "PUT",
    body: JSON.stringify({ settings }),
  });
}

export async function deleteWidget(
  dashboardId: string,
  widgetId: string
): Promise<{ status: string }> {
  return apiFetch<{ status: string }>(`/api/dashboards/${dashboardId}/widgets/${widgetId}`, {
    method: "DELETE",
  });
}
