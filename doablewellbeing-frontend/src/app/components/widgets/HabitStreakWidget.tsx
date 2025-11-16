// app/components/widgets/HabitStreakWidget.tsx
import type { WidgetProps } from "./index";
import { WidgetCard } from "./index";

export const HabitStreakWidget: React.FC<WidgetProps> = ({ widget }) => {
  const currentStreak = 7;
  const longestStreak = 21;

  return (
    <WidgetCard>
      <div className="flex flex-col gap-3">
        {/* Felső sor: Current vs Longest */}
        <div className="flex items-baseline justify-between">
          <div>
            <div className="text-[10px] uppercase tracking-[0.15em] text-slate-400">
              Current streak
            </div>
            <div className="mt-1 text-3xl font-bold text-emerald-400">
              {currentStreak}
              <span className="ml-1 text-base font-medium text-slate-300">
                days
              </span>
            </div>
          </div>

          <div className="text-right">
            <div className="text-[10px] uppercase tracking-[0.15em] text-slate-500">
              Longest streak
            </div>
            <div className="mt-1 text-base font-semibold text-slate-400">
              {longestStreak} days
            </div>
          </div>
        </div>

        {/* Thin divider */}
        <div className="h-px w-full bg-slate-800/80 my-1" />

        {/* Rövid magyarázó sor */}
        <p className="text-[11px] leading-snug text-slate-400">
          Keep the chain going by completing at least one tiny habit today.
        </p>
      </div>
    </WidgetCard>
  );
};
