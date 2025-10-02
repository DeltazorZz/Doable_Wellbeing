import CoachCardWithPlaceholder from "@/app/components/coachCard";
import Navbar from "@/app/components/navbar";
import React from "react";

const OurMissionPage = () => (
    <>
        <Navbar />
        <section className="bg-gradient-to-r from-blue-200 via-purple-100 to-pink-200 py-16">
            <div className="max-w-3xl mx-auto text-center px-6">
                <h1 className="text-5xl md:text-6xl font-extrabold mb-6 text-gray-900 drop-shadow-xl">
                    Our Mission
                </h1>
                <p className="mb-10 text-gray-700 text-xl md:text-2xl font-medium leading-relaxed">
                    We empower individuals to achieve wellbeing through personalized coaching, compassionate support, and accessible resources.
                </p>
            </div>
        </section>
        <main className="flex flex-wrap gap-10 justify-center p-10 bg-white min-h-screen rounded-t-3xl shadow-2xl -mt-12">
            {[...Array(6)].map((_, i) => (
                <div
                    key={i}
                    className="transition-transform hover:scale-105 hover:shadow-xl rounded-xl"
                >
                    <CoachCardWithPlaceholder />
                </div>
            ))}
        </main>
    </>
);

export default OurMissionPage;