import React, { useState } from "react";

export default function ReferralsSection() {
  const [formData, setFormData] = useState({
    mobile_phone: "",
    home_phone: "",
    presenting_problem: "",
    heard_about_us: "",
    title: "",
    gender_identity: "",
    is_gender_same_as_assigned_at_birth: false,
    marital_status: "",
    accommodation_type: "",
    employment_status: "",
    sexual_orientation: "",
    ethnic_origin: "",
    religion: "",
    first_language: "",
    requires_interpreter: false,
    english_difficulty: false,
    english_support_details: "",
    has_disability: false,
    has_long_term_conditions: false,
    has_armed_forces_affiliation: false,
    expecting_or_child_under_24m: false,
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    setFormData(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Form submitted:", formData);
  };

  return (
    <section className="text-black">
      <h1 className="text-2xl font-medium text-neutral-900 mb-6">Self-Referrals</h1>
      <form onSubmit={handleSubmit} className="space-y-6 max-w-2xl">
        
        {/* Contacts */}
        <fieldset className="border-b pb-6">
          <legend className="text-lg font-semibold text-neutral-800 mb-4">Contact Information</legend>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-neutral-700">Mobile Phone</label>
              <input type="tel" name="mobile_phone" value={formData.mobile_phone} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2" />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">Home Phone</label>
              <input type="tel" name="home_phone" value={formData.home_phone} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2" />
            </div>
          </div>
        </fieldset>

        {/* Presenting Problem */}
        <fieldset className="border-b pb-6">
          <legend className="text-lg font-semibold text-neutral-800 mb-4">Presenting Problem</legend>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-neutral-700">Presenting Problem</label>
              <textarea name="presenting_problem" value={formData.presenting_problem} onChange={handleChange} rows={4} className="w-full border border-neutral-300 rounded-lg p-2" />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">How did you hear about us?</label>
              <input type="text" name="heard_about_us" value={formData.heard_about_us} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2" />
            </div>
          </div>
        </fieldset>

        {/* About You */}
        <fieldset className="border-b pb-6">
          <legend className="text-lg font-semibold text-neutral-800 mb-4">About You</legend>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-neutral-700">Title</label>
              <select name="title" value={formData.title} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2">
                <option value="">Select...</option>
                <option>Mr</option><option>Mrs</option><option>Ms</option><option>Dr</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">Gender Identity</label>
              <select name="gender_identity" value={formData.gender_identity} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2">
                <option value="">Select...</option>
                <option>Male</option><option>Female</option><option>Non-binary</option>
              </select>
            </div>
            <div className="flex items-center">
              <input type="checkbox" id="same_gender" name="is_gender_same_as_assigned_at_birth" checked={formData.is_gender_same_as_assigned_at_birth} onChange={handleChange} className="mr-2" />
              <label htmlFor="same_gender" className="text-sm font-medium text-neutral-700">Same as assigned at birth</label>
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">Marital Status</label>
              <select name="marital_status" value={formData.marital_status} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2">
                <option value="">Select...</option>
                <option>Single</option><option>Married</option><option>Divorced</option><option>Widowed</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">Accommodation Type</label>
              <select name="accommodation_type" value={formData.accommodation_type} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2">
                <option value="">Select...</option>
                <option>Owner Occupied</option><option>Rented</option><option>Shared</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">Employment Status</label>
              <select name="employment_status" value={formData.employment_status} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2">
                <option value="">Select...</option>
                <option>Employed</option><option>Unemployed</option><option>Student</option><option>Retired</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">Sexual Orientation</label>
              <select name="sexual_orientation" value={formData.sexual_orientation} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2">
                <option value="">Select...</option>
                <option>Heterosexual</option><option>Homosexual</option><option>Bisexual</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">Ethnic Origin</label>
              <input type="text" name="ethnic_origin" value={formData.ethnic_origin} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2" />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">Religion</label>
              <input type="text" name="religion" value={formData.religion} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2" />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700">First Language</label>
              <input type="text" name="first_language" value={formData.first_language} onChange={handleChange} className="w-full border border-neutral-300 rounded-lg p-2" />
            </div>
            <div className="flex items-center">
              <input type="checkbox" id="interpreter" name="requires_interpreter" checked={formData.requires_interpreter} onChange={handleChange} className="mr-2" />
              <label htmlFor="interpreter" className="text-sm font-medium text-neutral-700">Requires interpreter</label>
            </div>
            <div className="flex items-center">
              <input type="checkbox" id="english_difficulty" name="english_difficulty" checked={formData.english_difficulty} onChange={handleChange} className="mr-2" />
              <label htmlFor="english_difficulty" className="text-sm font-medium text-neutral-700">English difficulty</label>
            </div>
            {formData.english_difficulty && (
              <div>
                <label className="block text-sm font-medium text-neutral-700">English Support Details</label>
                <textarea name="english_support_details" value={formData.english_support_details} onChange={handleChange} rows={2} className="w-full border border-neutral-300 rounded-lg p-2" />
              </div>
            )}
            <div className="space-y-2">
              <div className="flex items-center">
                <input type="checkbox" id="disability" name="has_disability" checked={formData.has_disability} onChange={handleChange} className="mr-2" />
                <label htmlFor="disability" className="text-sm font-medium text-neutral-700">Has disability</label>
              </div>
              <div className="flex items-center">
                <input type="checkbox" id="conditions" name="has_long_term_conditions" checked={formData.has_long_term_conditions} onChange={handleChange} className="mr-2" />
                <label htmlFor="conditions" className="text-sm font-medium text-neutral-700">Has long-term conditions</label>
              </div>
              <div className="flex items-center">
                <input type="checkbox" id="armed_forces" name="has_armed_forces_affiliation" checked={formData.has_armed_forces_affiliation} onChange={handleChange} className="mr-2" />
                <label htmlFor="armed_forces" className="text-sm font-medium text-neutral-700">Armed forces affiliation</label>
              </div>
              <div className="flex items-center">
                <input type="checkbox" id="child" name="expecting_or_child_under_24m" checked={formData.expecting_or_child_under_24m} onChange={handleChange} className="mr-2" />
                <label htmlFor="child" className="text-sm font-medium text-neutral-700">Expecting or child under 24 months</label>
              </div>
            </div>
          </div>
        </fieldset>

        <button type="submit" className="mt-6 bg-neutral-900 text-white px-4 py-2 rounded-lg hover:bg-neutral-800 transition-colors">
          Submit Referral
        </button>
      </form>
    </section>
  );
}