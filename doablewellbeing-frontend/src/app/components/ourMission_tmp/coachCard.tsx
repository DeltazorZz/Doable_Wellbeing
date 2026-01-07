"use client";

import React from "react";
import type { Coach } from "@/lib/api/coachApi";

export function CoachCard({ coach }: { coach: Coach }) {
  return (
    <div className="w-[320px] rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition-transform hover:scale-[1.02] hover:shadow-md">
      <div className="flex items-start gap-3">
        {/* avatar placeholder */}
        <div className="h-12 w-12 shrink-0 rounded-full bg-slate-200" />

        <div className="min-w-0">
          <div className="text-lg font-semibold text-slate-900 truncate">
            {coach.displayName}
          </div>
          {coach.expertise ? (
            <div className="mt-0.5 text-xs font-medium text-slate-600">
              {coach.expertise}
            </div>
          ) : (
            <div className="mt-0.5 text-xs text-slate-400">Expertise not set</div>
          )}
        </div>
      </div>

      <div className="mt-4 text-sm text-slate-700 leading-relaxed line-clamp-4">
        {coach.bio?.trim()
          ? coach.bio
          : "This coach hasn’t added a bio yet — but they’re ready to help you build better habits."}
      </div>

      {coach.timezone && (
        <div className="mt-4 inline-flex items-center rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-700">
          Timezone: {coach.timezone}
        </div>
      )}
    </div>
  );
}
