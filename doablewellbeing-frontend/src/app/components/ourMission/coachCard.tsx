import React from 'react';

type CoachCardProps = {
    profilePicUrl: string;
    title: string;
    name: string;
    bio: string;
};

const CoachCard: React.FC<CoachCardProps> = ({ profilePicUrl, title, name, bio }) => (
    <div className="border border-gray-200 rounded-xl p-5 max-w-xs shadow-md bg-white flex flex-col items-center">
        <img
            src={profilePicUrl}
            alt={`${name} profile`}
            className="w-20 h-20 rounded-full object-cover mb-4"
        />
        <div className="font-semibold text-base text-gray-600 mb-1">{title}</div>
        <div className="font-bold text-lg mb-2">{name}</div>
        <div className="text-sm text-gray-700 text-center">{bio}</div>
    </div>
);


const placeholderCoach = {
    profilePicUrl: "https://via.placeholder.com/80",
    title: "Wellbeing Coach",
    name: "Alex Smith",
    bio: "Passionate about helping others achieve their wellness goals. Experienced in mindfulness and nutrition."
};

// Example usage
export default function CoachCardWithPlaceholder() {
    return <CoachCard {...placeholderCoach} />;
}
