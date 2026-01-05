"use client";

interface SessionOptionsProps {
  goal: string;
  setGoal: (v: string) => void;
  notes: string;
  setNotes: (v: string) => void;
  emailReminder: boolean;
  setEmailReminder: (v: boolean) => void;
  extraNotesEnabled: boolean;
  setExtraNotesEnabled: (v: boolean) => void;
}

export function SessionOptions({
  goal,
  setGoal,
  notes,
  setNotes,
  emailReminder,
  setEmailReminder,
  extraNotesEnabled,
  setExtraNotesEnabled,
}: SessionOptionsProps) {
  return (
    <>
      <section>
        <label
          htmlFor="goal"
          className="mb-2 block text-sm font-semibold text-gray-800"
        >
          Do you have a goal for this session? Tell us!
        </label>
        <textarea
          id="goal"
          value={goal}
          onChange={(e) => setGoal(e.target.value)}
          rows={4}
          placeholder="e.g., I want to prepare for a stressful presentation next week."
          className="w-full resize-y rounded-xl border border-gray-300 bg-white p-3 shadow-sm outline-none transition focus:border-gray-900 text-black"
        />
      </section>

      <section className="space-y-4">
        <label className="flex items-center gap-3 text-gray-800">
          <input
            type="checkbox"
            className="h-5 w-5 rounded border-gray-300 text-gray-900 focus:ring-gray-900"
            checked={emailReminder}
            onChange={(e) => setEmailReminder(e.target.checked)}
          />
          <span>Send me an email reminder</span>
        </label>

        <label className="flex items-center gap-3 text-gray-800">
          <input
            type="checkbox"
            className="h-5 w-5 rounded border-gray-300 text-gray-900 focus:ring-gray-900"
            checked={extraNotesEnabled}
            onChange={(e) => setExtraNotesEnabled(e.target.checked)}
          />
          <span>Add extra notes (optional)</span>
        </label>

        {extraNotesEnabled && (
          <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            rows={3}
            placeholder="Anything else you'd like your coach to know?"
            className="mt-2 w-full resize-y rounded-xl border border-gray-300 bg-white p-3 shadow-sm outline-none transition focus:border-gray-900 text-black"
          />
        )}
      </section>
    </>
  );
}
