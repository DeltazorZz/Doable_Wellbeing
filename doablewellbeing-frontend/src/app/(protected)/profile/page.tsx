'use client';

import React, { useMemo, useState } from "react";
import Navbar from "@/app/components/navbar";
import PersonalSection from "@/app/components/profilePage/PersonalSection";
import ReferralsSection from "@/app/components/profilePage/ReferralSection";
import SettingsSection from "@/app/components/profilePage/SettingsSection";

type TabId = "personal" | "referrals" | "settings";

interface SidebarItem {
  id: TabId;
  title: string;
}

const sidebarItems: SidebarItem[] = [
  { id: "personal",  title: "Personal Data"   },
  { id: "referrals", title: "Self-Referrals"  },
  { id: "settings",  title: "Settings"        },
];

export default function ProfileEmbeddedPage() {
  // Egyszerű lokális state, default: "personal"
  const [active, setActive] = useState<TabId>("personal");

  const Content = useMemo(() => {
    switch (active) {
      case "personal":
        return <PersonalSection />;
      case "referrals":
        return <ReferralsSection />;
      case "settings":
        return <SettingsSection />;
      default:
        return <PersonalSection />;
    }
  }, [active]);

  const onSelect = (id: TabId) => {
    setActive(id);

  };

  return (
    <>
      <Navbar />
      <div className="flex justify-center items-start min-h-screen py-20">
        <div className="flex w-full h-full max-w-6xl">
          <aside className="w-64 border-r bg-white">
            <div className="px-5 py-6 border-b">
              <h2 className="text-xl font-semibold text-neutral-900">Profile</h2>
            </div>
            <nav className="px-3 py-4">
              {sidebarItems.map((item) => {
                const isActive = active === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => onSelect(item.id)}
                    className={`w-full text-left block px-4 py-2.5 mb-1 rounded-lg transition-colors text-lg ${
                      isActive
                        ? "bg-black text-white"
                        : "text-black hover:bg-gray-200 "
                    }`}
                  >
                    {item.title}
                  </button>
                );
              })}
            </nav>
          </aside>

          <main className="flex-1 px-10 py-8">
            <div className="max-w-3xl mx-auto">{Content}</div>
          </main>
        </div>
      </div>
    </>
  );
}
