// app/components/Navbar.tsx
"use client";

import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import { useState, useEffect } from "react";
import { Menu, X } from "lucide-react";

const NAV = [
  { href: "/", label: "Home" },
  { href: "/our-mission", label: "Our Mission" },
  { href: "/library", label: "Library" },
  { href: "/book-a-coach", label: "Book a coach" },
];

export default function Navbar({
  showTopBar = true,
  breadcrumb = "Home",
}: {
  showTopBar?: boolean;
  breadcrumb?: string;
}) {
  const pathname = usePathname();
  const [open, setOpen] = useState(false);

  // close on route change
  useEffect(() => setOpen(false), [pathname]);

  const isActive = (href: string) =>
    href === "/"
      ? pathname === "/"
      : pathname?.startsWith(href);

  return (
    <header className="w-full border-b">


      {/* Main nav */}
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex h-20 items-center justify-between">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-3">
            {/* Cseréld a /logo.png-t a sajátodra */}
            <Image
              src="/DoableLogo-noBg.png"
              alt="Doable Wellbeing"
              width={300}
              height={100}
              className="h-auto w-auto"
              priority
            />
          </Link>

          {/* Desktop nav */}
          <nav className="hidden items-center gap-8 md:flex">
            {NAV.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={[
                  "text-lg transition-colors",
                  isActive(item.href)
                    ? "text-black"
                    : "text-neutral-600 hover:text-black",
                ].join(" ")}
                aria-current={isActive(item.href) ? "page" : undefined}
              >
                {item.label}
              </Link>
            ))}

            {/* Right-aligned auth link */}
            <Link
              href="/auth/sign-in"
              className="ml-6 text-lg  text-neutral-900 hover:underline underline-offset-4">
              Sign in / SignUp
            </Link>
          </nav>

          {/* Mobile toggler */}
          <button
            className="md:hidden inline-flex items-center justify-center rounded-lg p-2 text-black  hover:text-pink-400"
            aria-label="Open menu"
            aria-expanded={open}
            onClick={() => setOpen((v) => !v)}
          >
            {open ? <X /> : <Menu />}
          </button>
        </div>
      </div>

      {/* Mobile drawer */}
      <div
        className={[
          "md:hidden border-t border-neutral-200 bg-white",
          open ? "block" : "hidden",
        ].join(" ")}
      >
        <nav className="mx-auto max-w-7xl px-4 py-4 flex flex-col gap-3">
          {NAV.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={[
                "py-2 text-base",
                isActive(item.href)
                  ? "font-medium text-black"
                  : "text-neutral-700",
              ].join(" ")}
              aria-current={isActive(item.href) ? "page" : undefined}
            >
              {item.label}
            </Link>
          ))}
          <Link
            href="/auth/sign-in"
            className="py-2 text-base underline underline-offset-4 text-neutral-900"
          >
            Sign in / SignUp
          </Link>
        </nav>
      </div>
    </header>
  );
}
