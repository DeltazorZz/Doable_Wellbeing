export default function SettingsSection() {
  return (
    <section className="text-black">
      <h1 className="text-2xl font-medium text-neutral-900 mb-6">Settings</h1>
      <form className="space-y-4 max-w-xl">
        <div className="flex items-center justify-between">
          <label htmlFor="emailNotifications" className="text-neutral-700">Email Notifications</label>
          <input id="emailNotifications" type="checkbox" />
        </div>
        <div className="flex items-center justify-between">
          <label htmlFor="smsReminders" className="text-neutral-700">SMS Reminders</label>
          <input id="smsReminders" type="checkbox" />
        </div>
        <div className="flex items-center justify-between">
          <span className="text-neutral-700">Request Data Export</span>
          <button type="button" className="bg-neutral-900 text-white px-3 py-1.5 rounded-lg hover:bg-neutral-800">
            Export
          </button>
        </div>
      </form>
    </section>
  );
}