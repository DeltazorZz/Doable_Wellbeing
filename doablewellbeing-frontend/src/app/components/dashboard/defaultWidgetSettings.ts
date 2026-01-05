export function getDefaultWidgetSettings(moduleCode: string) {
  switch (moduleCode) {
    case "upcoming_meetings":
      return { showDaysAhead: 14 };

    case "completed_meetings":
      return { maxItems: 5 };

    case "habit_tracker":
      return { showMax: 6 };

    case "mood_chart":
      return { rangeDays: 14 };

    case "wheel_of_life":
      return { showLabels: true };

    case "goals_progress":
      return { showMax: 5 };

    case "quick_checkin":
      return { variant: "comfort_stretch_burnout" }; 

    case "micro_habits":
      return { category: "MENTAL", showMax: 5 };

    default:
      return {};
  }
}
