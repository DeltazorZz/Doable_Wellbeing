import React from "react";
import { HabitStreakWidget } from "./HabitStreakWidget";
// import { MoodSparklineWidget } from "./MoodSparklineWidget";
// import { WheelOfLifeWidget } from "./WheelOfLifeWidget";
// import { GoalProgressWidget } from "./GoalProgressWidget";
// import { SandTimerWidget } from "./SandTimerWidget";

export type WidgetType =
  | "HabitStreak"
//   | "MoodSparkline"
//   | "WheelOfLife"
//   | "GoalProgress"
//   | "SandTimer";


export interface WidgetInstance {
  id: string;
  type: WidgetType;
  title?: string;

}

export interface WidgetProps {
  widget: WidgetInstance;
}

export const WidgetRegistry: Record<WidgetType, React.FC<WidgetProps>> = {
  HabitStreak: HabitStreakWidget,
//   MoodSparkline: MoodSparklineWidget,
//   WheelOfLife: WheelOfLifeWidget,
//   GoalProgress: GoalProgressWidget,
//   SandTimer: SandTimerWidget,
};

export const WidgetCard: React.FC<
  React.PropsWithChildren<{ title?: string }>> = ({ title, children }) => {
return (
    <div className="h-full w-full rounded-xl border border-slate-800/70 bg-slate-950/70 shadow-inner flex flex-col overflow-hidden">
      <div className="flex-1 px-4 py-3 text-sm text-slate-100">
        {children}
      </div>
    </div>
  );
}
