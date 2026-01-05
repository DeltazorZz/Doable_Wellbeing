"use client";
import type { DashboardWidgetView } from "@/types/dashboard";

import { UpcomingMeetingsWidget } from "./UpcomingMeetingsWidget";
import { CompletedMeetingsWidget } from "./CompletedMeetingsWidget";
import { HabitTrackerWidget } from "./HabitTrackerWidget";
import { MicroHabitsWidget } from "./MicroHabitsWidget";
import { MoodChartWidget } from "./MoodChartWidget";
import { WheelOfLifeWidget } from "./WheelOfLifeWidget";
import { GoalsProgressWidget } from "./GoalsProgressWidget";


export function WidgetBody({ widget }: { widget: DashboardWidgetView }) {
  switch (widget.moduleCode) {
    case "upcoming_meetings":
      return <UpcomingMeetingsWidget widget={widget} />;
    case "completed_meetings":
      return <CompletedMeetingsWidget widget={widget} />;
    case "habit_tracker":
      return <HabitTrackerWidget widget={widget} />;
    case "micro_habits":
      return <MicroHabitsWidget widget={widget} />;
    case "mood_chart":
      return <MoodChartWidget widget={widget} />;
    case "wheel_of_life":
      return <WheelOfLifeWidget widget={widget} />;
    case "goals_progress":
      return <GoalsProgressWidget widget={widget} />;

    default:
      return <div className="text-sm text-gray-700">🧩 {widget.moduleCode}</div>;
  }
}
