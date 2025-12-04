"use client";

import React, { useState } from 'react';
import Link from 'next/link';

interface SidebarItem {
    id: string;
    title: string;
    icon?: React.ReactNode;
}

const sidebarItems: SidebarItem[] = [
    { id: 'personal', title: 'Personal Data' },
    { id: 'referrals', title: 'Self-Referrals' },
    { id: 'settings', title: 'Settings' },
];

const ProfileSidebar = () => {
    const [activeItem, setActiveItem] = useState('personal');

    return (
        <div className="flex min-h-screen bg-white">
            {/* Sidebar */}
            <div className="w-64 border-r bg-white">
                <div className="px-4 py-6">
                    <h2 className="text-xl font-medium text-neutral-900">Profile</h2>
                </div>
                <nav className="px-2">
                    {sidebarItems.map((item) => (
                        <Link
                            key={item.id}
                            href={`/profile/${item.id}`}
                            className={`block px-4 py-2.5 mb-1 rounded-lg transition-colors text-lg ${
                                activeItem === item.id
                                    ? 'bg-neutral-900 text-white'
                                    : 'text-neutral-600 hover:bg-neutral-100 hover:text-black'
                            }`}
                            onClick={() => setActiveItem(item.id)}
                        >
                            {item.title}
                        </Link>
                    ))}
                </nav>
            </div>

            {/* Main Content Area */}
            <div className="flex-1 px-8 py-6">
                {activeItem === 'personal' && (
                    <div>
                        <h1 className="text-2xl font-medium text-neutral-900 mb-6">Personal Data</h1>
                        {/* Add personal data form or content here */}
                    </div>
                )}

                {activeItem === 'referrals' && (
                    <div>
                        <h1 className="text-2xl font-medium text-neutral-900 mb-6">Self-Referrals</h1>
                        {/* Add referrals content here */}
                    </div>
                )}

                {activeItem === 'settings' && (
                    <div>
                        <h1 className="text-2xl font-medium text-neutral-900 mb-6">Settings</h1>
                        {/* Add settings content here */}
                    </div>
                )}
            </div>
        </div>
    );
};

export default ProfileSidebar;