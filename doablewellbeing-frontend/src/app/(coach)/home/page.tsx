"use client";

import React, { useState } from "react";
import Navbar from "@/app/components/navbar";
import { AvailabilityTab } from "@/app/components/coach/Availability/AvailabilityTab";
import { CoachCalendar } from "@/app/components/coach/Overview/CoachCalendar";


export default function CoachPage() {
  const [activeTab, setActiveTab] = useState<"overview" | "availability">(
    "overview"
  );

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <Navbar />
      <main className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-6 lg:px-8 lg:py-8">
        <header className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <h1 className="text-2xl font-semibold text-slate-900">
              Coach — Wellbeing Coaching
            </h1>
            <p className="text-sm text-slate-500">
              Manage your clients, sessions and weekly availability
            </p>
          </div>

          <div className="flex items-center gap-2 text-xs font-medium text-slate-500">
            <button
              type="button"
              onClick={() => setActiveTab("overview")}
              className={[
                "rounded-full px-3 py-1 border transition",
                activeTab === "overview"
                  ? "border-teal-500 bg-teal-50 text-teal-700"
                  : "border-transparent hover:border-teal-200 hover:bg-teal-50/50",
              ].join(" ")}
            >
              Overview
            </button>
            <button
              type="button"
              onClick={() => setActiveTab("availability")}
              className={[
                "rounded-full px-3 py-1 border transition",
                activeTab === "availability"
                  ? "border-teal-500 bg-teal-50 text-teal-700"
                  : "border-transparent hover:border-teal-200 hover:bg-teal-50/50",
              ].join(" ")}
            >
              Weekly availability
            </button>
          </div>
        </header>

        {activeTab === "overview" && <CoachCalendar />}
        {activeTab === "availability" && <AvailabilityTab />}
      </main>
    </div>
  );
}