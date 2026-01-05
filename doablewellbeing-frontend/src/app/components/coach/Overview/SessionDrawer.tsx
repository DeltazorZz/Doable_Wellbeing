"use client";

import React, { useState } from "react";
import { CoachCalendarEventView } from "./types";

export function SessionDrawer({
  open,
  event,
  onClose,
  loading,
  onConfirm,
  onDecline,
  onComplete,
  onCancel,
}: {
  open: boolean;
  event: CoachCalendarEventView | null;
  onClose: () => void;
  loading: boolean;
  onConfirm: (id: string) => Promise<void>;
  onDecline: (id: string, reason: string) => Promise<void>;
  onComplete: (id: string) => Promise<void>;
  onCancel: (id: string) => Promise<void>;
}) {
  const [reason, setReason] = useState("");

  if (!open || !event) return null;

  const canConfirm = event.status === "requested";
  const canDecline = event.status === "requested";
  const canComplete = event.status === "scheduled";
  const canCancel = event.status === "requested" || event.status === "scheduled";

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onMouseDown={onClose}
    >
      <div
        className="w-full max-w-lg rounded-2xl bg-white p-5 shadow-xl ring-1 ring-slate-200"
        onMouseDown={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between gap-3">
          <div>
            <div className="text-base font-semibold text-slate-900">{event.title}</div>
            <div className="mt-1 text-sm text-slate-500">{event.startsAt}</div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg px-2 py-1 text-sm text-slate-500 hover:bg-slate-100"
          >
            ✕
          </button>
        </div>

        <div className="mt-4 space-y-2 text-sm">
          <InfoRow label="Status" value={event.status} />
          <InfoRow label="Client" value={event.client.email ?? "—"} />
          <InfoRow
            label="Meet"
            value={
              event.meetingUrl ? (
                <a className="text-teal-700 underline" href={event.meetingUrl} target="_blank" rel="noreferrer">
                  Open link
                </a>
              ) : (
                "—"
              )
            }
          />
        </div>

        {event.notesPreview && (
          <div className="mt-4 rounded-xl bg-slate-50 p-3 text-sm text-slate-700">
            {event.notesPreview}
          </div>
        )}

        <div className="mt-5 space-y-2">
          {canConfirm && (
            <button
              disabled={loading}
              onClick={() => onConfirm(event.id)}
              className="w-full rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-black disabled:opacity-50"
            >
              Confirm
            </button>
          )}

          {canComplete && (
            <button
              disabled={loading}
              onClick={() => onComplete(event.id)}
              className="w-full rounded-xl bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              Mark as completed
            </button>
          )}

          {canDecline && (
            <div className="rounded-xl border border-slate-200 p-3">
              <label className="text-xs font-semibold text-slate-700">Decline reason</label>
              <input
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="e.g. Not available"
                className="mt-2 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-teal-500"
              />
              <button
                disabled={loading || reason.trim().length < 3}
                onClick={() => onDecline(event.id, reason.trim())}
                className="mt-2 w-full rounded-xl bg-rose-600 px-4 py-2 text-sm font-semibold text-white hover:bg-rose-700 disabled:opacity-50"
              >
                Decline
              </button>
            </div>
          )}

          {canCancel && (
            <button
              disabled={loading}
              onClick={() => onCancel(event.id)}
              className="w-full rounded-xl border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50 disabled:opacity-50"
            >
              Cancel session
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-3">
      <div className="text-slate-500">{label}</div>
      <div className="text-right font-medium text-slate-900">{value}</div>
    </div>
  );
}
