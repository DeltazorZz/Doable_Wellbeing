"use client";

import Navbar from "@/app/components/navbar";
import React, { useMemo, useState } from "react";
import { BookOpen, ExternalLink, Search, Bookmark } from "lucide-react";

type Resource = {
  title: string;
  description: string;
  link: string;
  tags: string[];
};

const ALL_TAGS = ["Mindfulness", "Stress", "Sleep", "Activity", "Basics"];

const resources: Resource[] = [
  {
    title: "Mindfulness Meditation Guide",
    description: "A simple guide to start practicing mindfulness meditation.",
    link: "https://www.mindful.org/how-to-meditate/",
    tags: ["Mindfulness", "Basics"],
  },
  {
    title: "Stress Management Tips",
    description: "Practical tips to manage and reduce stress in daily life.",
    link: "https://www.helpguide.org/articles/stress/stress-management.htm",
    tags: ["Stress", "Basics"],
  },
  {
    title: "Healthy Sleep Habits",
    description: "Learn about habits that promote better sleep.",
    link: "https://www.sleepfoundation.org/sleep-hygiene",
    tags: ["Sleep", "Basics"],
  },
  {
    title: "Physical Activity Guidelines",
    description: "Recommendations for staying active and healthy.",
    link: "https://www.cdc.gov/physicalactivity/basics/index.htm",
    tags: ["Activity", "Basics"],
  },
];

export default function LibraryPage() {
  const [query, setQuery] = useState("");
  const [activeTag, setActiveTag] = useState<string | null>(null);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return resources.filter((r) => {
      const matchesQuery =
        !q ||
        r.title.toLowerCase().includes(q) ||
        r.description.toLowerCase().includes(q);
      const matchesTag = !activeTag || r.tags.includes(activeTag);
      return matchesQuery && matchesTag;
    });
  }, [query, activeTag]);

  return (
    <>
      <Navbar />
      <main className="relative isolate">
        {/* soft background */}
        <div aria-hidden className="pointer-events-none absolute inset-x-0 top-0 -z-10 overflow-hidden">
          <div className="mx-auto h-56 max-w-6xl blur-2xl">
            <div className="mx-8 mt-8 h-48 rounded-full bg-gradient-to-r from-sky-100 via-emerald-100 to-indigo-100" />
          </div>
        </div>

        <section className="mx-auto max-w-5xl px-4 pt-10 pb-6 sm:pt-12">
          <div className="text-center">
            <div className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white/70 px-3 py-1 text-xs font-medium text-slate-600 shadow-sm backdrop-blur">
              <Bookmark className="h-3.5 w-3.5" />
              Free, curated resources
            </div>

            <h1 className="mt-4 text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">
              Free Wellbeing Resources
            </h1>
            <p className="mx-auto mt-2 max-w-2xl text-slate-600">
              Practical guides you can use today—mindfulness, stress, sleep and movement.
            </p>
          </div>

          {/* search + tags */}
          <div className="mt-8 flex flex-col items-stretch justify-between gap-4 sm:flex-row sm:items-center">
            <label className="group relative w-full sm:max-w-md">
              <span className="sr-only">Search resources</span>
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400 group-focus-within:text-slate-600" />
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search guides (e.g. sleep, stress, breathing)…"
                className="w-full rounded-lg border border-slate-200 bg-white px-9 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 shadow-sm outline-none transition focus:border-sky-300 focus:ring-0 focus:shadow-md"
              />
            </label>

            <div className="flex flex-wrap items-center gap-2">
              <button
                onClick={() => setActiveTag(null)}
                className={`rounded-full border px-3 py-1.5 text-xs font-medium transition ${
                  activeTag === null
                    ? "border-sky-200 bg-sky-50 text-sky-700"
                    : "border-slate-200 bg-white text-slate-700 hover:border-slate-300"
                }`}
              >
                All
              </button>
              {ALL_TAGS.map((t) => (
                <button
                  key={t}
                  onClick={() => setActiveTag((cur) => (cur === t ? null : t))}
                  className={`rounded-full border px-3 py-1.5 text-xs font-medium transition ${
                    activeTag === t
                      ? "border-emerald-200 bg-emerald-50 text-emerald-700"
                      : "border-slate-200 bg-white text-slate-700 hover:border-slate-300"
                  }`}
                >
                  {t}
                </button>
              ))}
            </div>
          </div>
        </section>

        {/* grid */}
        <section className="mx-auto max-w-5xl px-4 pb-14">
          {filtered.length === 0 ? (
            <div className="rounded-xl border border-dashed border-slate-300 p-10 text-center">
              <p className="text-sm text-slate-600">
                No matching resources. Try a different keyword or tag.
              </p>
            </div>
          ) : (
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {filtered.map((resource) => (
                <a
                  key={resource.title}
                  href={resource.link}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="group relative block overflow-hidden rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-200"
                >
                  <div className="flex items-start gap-3">
                    <div className="rounded-lg bg-sky-50 p-2 text-sky-700 ring-1 ring-sky-100 transition group-hover:bg-sky-100">
                      <BookOpen className="h-5 w-5" />
                    </div>
                    <div>
                      <h2 className="text-base font-semibold text-slate-900">
                        {resource.title}
                      </h2>
                      <p className="mt-1 line-clamp-3 text-sm text-slate-600">
                        {resource.description}
                      </p>
                    </div>
                  </div>

                  <div className="mt-4 flex flex-wrap items-center gap-2">
                    {resource.tags.map((tag) => (
                      <span
                        key={tag}
                        className="rounded-full border border-slate-200 bg-slate-50 px-2.5 py-1 text-[11px] font-medium text-slate-600"
                      >
                        {tag}
                      </span>
                    ))}
                  </div>

                  <div className="mt-4 flex items-center gap-1 text-sm font-medium text-sky-700 transition group-hover:gap-1.5">
                    Open resource
                    <ExternalLink className="h-4 w-4" />
                  </div>

                  {/* subtle hover glow (very light) */}
                  <div className="pointer-events-none absolute inset-0 opacity-0 transition group-hover:opacity-100">
                    <div className="absolute -inset-x-6 -top-24 h-40 bg-gradient-to-b from-sky-100/70 to-transparent blur-2xl" />
                  </div>
                </a>
              ))}
            </div>
          )}
        </section>

        {/* CTA strip – extra light */}
        <aside className="border-t border-slate-200 bg-gradient-to-b from-slate-50 to-white py-8">
          <div className="mx-auto flex max-w-5xl flex-col items-center justify-between gap-4 px-4 text-center sm:flex-row sm:text-left">
            <p className="text-sm text-slate-700">
              Missing a great, free resource? Help us keep the library useful.
            </p>
            <a
              href="mailto:hello@doablewellbeing.co.uk?subject=Resource%20suggestion"
              className="inline-flex items-center justify-center rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-slate-800"
            >
              Suggest a resource
            </a>
          </div>
        </aside>
      </main>
    </>
  );
}
