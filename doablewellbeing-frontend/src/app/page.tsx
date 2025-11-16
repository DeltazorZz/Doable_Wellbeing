'use client';
import Image from 'next/image';
import Navbar from './components/navbar';
import RotatingQuestions from './components/rotatingQuestions';
import CustomButton from './components/customButton';

export default function Home() {
  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-4xl px-4 py-10 text-black">

        <h1 className="mb-6 text-2xl font-semibold md:text-3xl">
          <span>Better <span className='text-pink-400 font-roboto'>Wellbeing</span> one step at a time</span>
        </h1>


        <div className="mx-auto mb-8 w-full">
          <Image
            src="/peopledoable.png"
            alt="Doable Wellbeing people collage"
            width={1600}
            height={900}
            className="h-auto w-full rounded-md border border-black/10 object-cover shadow-sm"
            priority
          />
        </div>

        
        <RotatingQuestions />


        <div className="mt-6 flex w-full flex-col items-center justify-center gap-4 md:mt-8 md:flex-row md:gap-6">
          <CustomButton variant="outline" size="lg" href="/book-a-coach"> Book a coach now </CustomButton>
          <CustomButton variant="primary" size="lg" href="/library"> Browse free resources </CustomButton>
        </div>


        <p className="mt-10 text-center text-lg font-medium">
          Ready to take your next step in wellbeing?
        </p>
      </main>
    </>
  );
}
