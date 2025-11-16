export default function PersonalSection() {
  return (
    <section className="text-black">
      <h1 className="text-2xl font-medium text-neutral-900 mb-6">Personal Data</h1>
      <form className="space-y-4 max-w-xl">
        <div>
          <label className="block text-sm font-medium text-neutral-700">First Name</label>
          <input className="w-full border border-neutral-300 rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-neutral-900" />
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700">Last Name</label>
          <input className="w-full border border-neutral-300 rounded-lg p-2" />
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700">Date of Birth</label>
          <input type="date" className="w-full border border-neutral-300 rounded-lg p-2" />
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700">Gender Identity</label>
          <select className="w-full border border-neutral-300 rounded-lg p-2">
            <option value="">Select...</option>
            <option>Male</option><option>Female</option><option>Non-binary</option><option>Other</option>
          </select>
        </div>
        <button type="submit" className="mt-4 bg-neutral-900 text-white px-4 py-2 rounded-lg hover:bg-neutral-800 transition-colors">
          Save Changes
        </button>
      </form>
    </section>
  );
}