'use client';

import React, { useEffect, useState } from 'react';

const questions = [
  'How can you feel well if you feel unsafe?',
  'How are you feeling today?',
  'What are you grateful for today?',
  'Did something make you smile today?',
  'What would you like to improve this week?',
  'What is most important to you right now?',
  'What small joy did you experience today?',
];

const ROTATE_INTERVAL = 1000;

const RotatingQuestions: React.FC = () => {
  const [current, setCurrent] = useState(0);

  useEffect(() => {
    const id = setInterval(
      () => setCurrent((p) => (p + 1) % questions.length),
      ROTATE_INTERVAL
    );
    return () => clearInterval(id);
  }, []);

  
  const dotsToShow = questions.length;

  return (
    <section className="flex justify-center py-6 md:py-8">
      <div
        className="flex w-full flex-col items-center rounded-[40px] bg-blue-400 px-6 py-5 text-center shadow-xl "
        role="status"
        aria-live="polite"
      >
        <span className="mb-3 text-base font-normal text-black md:text-lg">
          &ldquo;{questions[current]}&rdquo;
        </span>
        <div className="mt-1 flex gap-2">
          {Array.from({ length: dotsToShow }).map((_, idx) => {
            const active = current % dotsToShow === idx;
            return (
              <span
                key={idx}
                className={`inline-block h-2.5 w-2.5 rounded-full ${
                  active ? 'bg-blue-500' : 'bg-white'
                }`}
              />
            );
          })}
        </div>
      </div>
    </section>
  );
};

export default RotatingQuestions;
