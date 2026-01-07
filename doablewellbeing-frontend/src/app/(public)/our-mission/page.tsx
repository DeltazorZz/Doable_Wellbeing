import Navbar from "@/app/components/navbar";
import { fetchCoaches, type Coach} from "@/lib/api/coachApi";


export default async function OurMissionPage() {
  let coaches: Coach[] = [];
  try {  
    coaches = await fetchCoaches();
  } catch (e) {
    // ha az apiFetch dob hibát, itt csak lekezeljük UI szinten
    coaches = [];
  }

  const validCoaches = (coaches ?? []).filter(
    (c) => c?.id && c?.displayName
  );

  return (
    <>
      <Navbar />

      <section className="bg-gradient-to-r from-blue-200 via-purple-100 to-pink-200 py-16">
        <div className="max-w-3xl mx-auto text-center px-6">
          <h1 className="text-5xl md:text-6xl font-extrabold mb-6 text-gray-900 drop-shadow-xl">
            Our Mission
          </h1>
          <p className="mb-10 text-gray-700 text-xl md:text-2xl font-medium leading-relaxed">
            We empower individuals to achieve wellbeing through personalized coaching,
            compassionate support, and accessible resources.
          </p>
        </div>
      </section>

      <main className="p-10 bg-white min-h-screen rounded-t-3xl shadow-2xl -mt-12">
          <div className="max-w-2xl mx-auto rounded-2xl border border-slate-200 bg-slate-50 p-8 text-center">
            <div className="text-lg font-semibold text-slate-900">No coaches available</div>
            <div className="mt-2 text-sm text-slate-600">
              Please check back later — we’re onboarding more coaches.
            </div>
          </div>
      </main>
    </>
  );
}
