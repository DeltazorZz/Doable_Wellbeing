"use client";

import React, { useMemo, useState } from "react";

type Client = {
  id: string;
  name: string;
  email?: string;
  goal?: string;
  joinedAt: string;
};

type Session = {
  id: string;
  clientId: string;
  date: string;
  coach: string;
  status: "upcoming" | "completed" | "cancelled";
  notes?: string;
};

const nowISO = () => new Date().toISOString();

const seedClients: Client[] = [/* ... */];
const seedSessions: Session[] = [/* ... */];

function uid(prefix = "") {
  return prefix + Math.random().toString(36).slice(2, 9);
}

export function OverviewTab() {
  const [clients, setClients] = useState<Client[]>(seedClients);
  const [sessions, setSessions] = useState<Session[]>(seedSessions);


  const stats = useMemo(() => {
    const totalClients = clients.length;
    const upcoming = sessions.filter((s) => s.status === "upcoming").length;
    const completed = sessions.filter((s) => s.status === "completed").length;
    return { totalClients, upcoming, completed };
  }, [clients, sessions]);

  const upcomingSessions = sessions
    .filter((s) => s.status === "upcoming")
    .sort((a, b) => +new Date(a.date) - +new Date(b.date));

  return (
    <>
      <section className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <StatCard label="Clients" value={stats.totalClients.toString()} />
        <StatCard
          label="Upcoming sessions"
          value={stats.upcoming.toString()}
        />
        <StatCard
          label="Completed sessions"
          value={stats.completed.toString()}
        />
      </section>
    </>
  );
}

function StatCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg bg-white px-4 py-3 shadow-sm ring-1 ring-slate-200">
      <div className="text-xs font-medium text-slate-500">{label}</div>
      <div className="text-xl font-bold text-slate-900">{value}</div>
    </div>
  );
}
