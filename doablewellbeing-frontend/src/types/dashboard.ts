export type Breakpoint = "lg" | "md" | "sm" | "xs" | "xxs";

export type PlacementView = {
  x: number;
  y: number;
  w: number;
  h: number;
  minW?: number | null;
  minH?: number | null;
  maxW?: number | null;
  maxH?: number | null;
  isStatic?: boolean | null;
};

export type DashboardWidgetView = {
  id: string;
  moduleCode: string;
  title?: string | null;
  settings: any; 
  isActive: boolean;
  placements: Partial<Record<Breakpoint, PlacementView>>;
};

export type DashboardView = {
  dashboardId: string;
  name: string;
  isDefault: boolean;
  widgets: DashboardWidgetView[];
};

export type ModuleView = {
  id: string;
  code: string;
  name: string;
  description?: string | null;
};

export type ResourceFile = {
  id: string;
  fileName: string;
  sizeLabel: string;
};

export type CompletedMeeting = {
  id: string;
  dateLabel: string;
  title: string;
  coachSummary?: string;
  files: ResourceFile[];
};
export type UpcomingMeetingsWidgetData = {
  showDaysAhead: number;
  meetings: Array<{
    id: string;
    title: string;
    startsAt: string;
    endsAt: string;
    status: string;
    meetingUrl?: string | null;
  }>;
};