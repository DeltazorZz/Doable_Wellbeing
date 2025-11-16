import { request } from "./client";

export type Breakpoint = "lg" | "md" | "sm" | "xs" | "xxs";

export interface LayoutsApiDto {
  lg: string | null;
  md: string | null;
  sm: string | null;
  xs: string | null;
  xxs: string | null;
}

export interface WidgetApiDto {
  id: string;         
  type: string;
  title: string | null;
  settings: string | null; 
}

export interface DashboardApiDto {
  name: string | null;
  layouts: LayoutsApiDto;
  widgets: WidgetApiDto[];
}

export function getDashboard() {
  return request<DashboardApiDto>({
    method: "GET",
    url: "api/dashboard",
  });
}

/**
 * PUT /api/dashboard
 */
export function saveDashboard(dto: DashboardApiDto) {
  return request<DashboardApiDto, DashboardApiDto>({
    method: "PUT",
    url: "api/dashboard",
    body: dto,
  });
}
