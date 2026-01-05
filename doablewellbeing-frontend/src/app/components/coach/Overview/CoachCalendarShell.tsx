// "use client";

// import React, { useMemo, useState } from "react";
// import { MiniMonthPicker } from "./MiniMonthPicker";
// import { AgendaList } from "./AgendaList";
// import { SessionDrawer } from "./SessionDrawer";
// import { useCoachCalendarUi } from "./useCoachCalendarUi";

// export function CoachCalendarShell() {
//   const {
//     selectedDate,
//     setSelectedDate,
//     dayEvents,
//     selectedEvent,
//     selectEvent,
//     closeDrawer,
//     stats,
//     loading,
//     actions,
//   } = useCoachCalendarUi();

//   const subtitle = useMemo(() => {
//     const d = new Date(selectedDate);
//     return d.toLocaleDateString(undefined, { weekday: "long", year: "numeric", month: "long", day: "numeric" });
//   }, [selectedDate]);

//   return (
//     <div className="grid gap-4 lg:grid-cols-[280px_1fr_380px]">
//       {/* Left */}
//       <aside className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
//         <div className="mb-3">
//           <div className="text-sm font-semibold text-slate-900">Calendar</div>
//           <div className="text-xs text-slate-500">Pick a day, manage requests.</div>
//         </div>

//         <MiniMonthPicker value={selectedDate} onChange={setSelectedDate} />

//         <div className="mt-4 space-y-2">
//           <QuickStat label="Requests" value={stats.requested} />
//           <QuickStat label="Today" value={stats.today} />
//           <QuickStat label="This week" value={stats.week} />
//         </div>

//         <div className="mt-4 rounded-xl border border-slate-200 bg-slate-50 p-3 text-xs text-slate-600">
//           Tip: click a session to open actions on the right.
//         </div>
//       </aside>

//       {/* Middle */}
//       <main className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
//         <header className="mb-4 flex items-start justify-between gap-3">
//           <div>
//             <h2 className="text-lg font-semibold text-slate-900">Agenda</h2>
//             <p className="text-sm text-slate-500">{subtitle}</p>
//           </div>

//           <div className="flex items-center gap-2">
//             {loading ? (
//               <span className="text-xs text-slate-500">Loading…</span>
//             ) : (
//               <span className="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-700">
//                 {dayEvents.length} sessions
//               </span>
//             )}
//           </div>
//         </header>

//         <AgendaList
//           events={dayEvents}
//           selectedId={selectedEvent?.id ?? null}
//           onSelect={selectEvent}
//         />
//       </main>

//       {/* Right */}
//       <section className="lg:block">
//         <SessionDrawer
//           event={selectedEvent}
//           onClose={closeDrawer}
//           loading={actions.loading}
//           onConfirm={actions.confirm}
//           onDecline={actions.decline}
//           onComplete={actions.complete}
//           onCancel={actions.cancel}
//         />
//       </section>
//     </div>
//   );
// }

// function QuickStat({ label, value }: { label: string; value: number }) {
//   return (
//     <div className="flex items-center justify-between rounded-xl border border-slate-200 bg-white px-3 py-2">
//       <div className="text-xs font-medium text-slate-600">{label}</div>
//       <div className="text-sm font-semibold text-slate-900">{value}</div>
//     </div>
//   );
// }
