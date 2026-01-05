
import type { Metadata } from "next";
import "./globals.css";
import { Geist, Geist_Mono, Roboto_Mono } from "next/font/google";
import { ReactNode } from "react";


const geistSans = Geist({
   variable: "--font-geist-sans",
   subsets: ["latin"],
 });

 const geistMono = Geist_Mono({
   variable: "--font-geist-mono",
   subsets: ["latin"],
 });
 const robotoMono = Roboto_Mono({
  variable: "--font-roboto-mono",
  subsets: ["latin"],
 })
export const metadata: Metadata = {
  title: "Doable Wellbeing",
  description: "Wellbeing coaching platform",
};


export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>
      
        {children}
      </body>
    </html>
  );
}


