// app/components/Navbar.tsx
"use client";

import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import { useState, useEffect } from "react";
import { Menu, X } from "lucide-react";
import { api } from "@/lib/api"; 

type User = {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
};

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
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  // route change → zárja be a menüt
  useEffect(() => setOpen(false), [pathname]);

  // betöltjük a felhasználót az auth cookie alapján
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const r = await api.get<User>("/auth/me", { withCredentials: true });
        setUser(r.data);
      } catch {
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    fetchUser();
  }, []);

  // kijelentkezés
  const handleLogout = async () => {
    try {
      await api.post("/auth/logout", {}, { withCredentials: true });
    } catch {}
    setUser(null);
  };

  const isActive = (href: string) =>
    href === "/" ? pathname === "/" : pathname?.startsWith(href);

  // extra menüpontok bejelentkezve
  const extraNav = [
    { href: "/dashboard", label: "Dashboard" },
    { href: "/habits", label: "Habits" },
    { href: "/appointments", label: "Appointments" },
  ];

  if (user?.roles?.includes("admin")) {
    extraNav.push({ href: "/admin", label: "Admin" });
  }

  return (
    <header className="w-full border-b bg-white py-10">
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex h-20 items-center justify-between">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-3">
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
                className={`text-lg transition-colors ${
                  isActive(item.href)
                    ? "text-black"
                    : "text-neutral-600 hover:text-black"
                }`}
              >
                {item.label}
              </Link>
            ))}

            {user &&
              extraNav.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`text-lg transition-colors ${
                    isActive(item.href)
                      ? "text-black"
                      : "text-neutral-600 hover:text-black"
                  }`}
                >
                  {item.label}
                </Link>
              ))}

            {/* Right-aligned auth/profile link */}
            {loading ? (
              <div className="ml-6 h-6 w-20 rounded bg-neutral-200 animate-pulse" />
            ) : user ? (
              <div className="ml-6 flex items-center gap-3">
                <Link
                  href="/profile"
                  className="text-lg font-medium text-neutral-900 hover:underline underline-offset-4"
                >
                  {user.firstName ?? "Profile"}
                </Link>
                <button
                  onClick={handleLogout}
                  className="rounded-lg bg-neutral-900 text-white px-3 py-1.5 text-sm hover:bg-black"
                >
                  Sign out
                </button>
              </div>
            ) : (
              <Link
                href="/login"
                className="ml-6 text-lg text-neutral-900 hover:underline underline-offset-4"
              >
                Sign in / Sign up
              </Link>
            )}
          </nav>

          {/* Mobile menu toggler */}
          <button
            className="md:hidden inline-flex items-center justify-center rounded-lg p-2 text-black hover:text-indigo-500"
            aria-label="Open menu"
            aria-expanded={open}
            onClick={() => setOpen((v) => !v)}
          >
            {open ? <X /> : <Menu />}
          </button>
        </div>
      </div>

      {/* Mobile drawer */}
      {open && (
        <div className="md:hidden border-t border-neutral-200 bg-white">
          <nav className="mx-auto max-w-7xl px-4 py-4 flex flex-col gap-3">
            {NAV.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`py-2 text-base ${
                  isActive(item.href)
                    ? "font-medium text-black"
                    : "text-neutral-700"
                }`}
              >
                {item.label}
              </Link>
            ))}

            {user &&
              extraNav.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`py-2 text-base ${
                    isActive(item.href)
                      ? "font-medium text-black"
                      : "text-neutral-700"
                  }`}
                >
                  {item.label}
                </Link>
              ))}

            {loading ? (
              <div className="h-6 w-24 bg-neutral-200 animate-pulse rounded" />
            ) : user ? (
              <>
                <Link
                  href="/profile"
                  className="py-2 text-base font-medium text-neutral-900 underline underline-offset-4"
                >
                  {user.firstName ?? "Profile"}
                </Link>
                <button
                  onClick={handleLogout}
                  className="py-2 text-base text-left text-red-600 hover:underline"
                >
                  Sign out
                </button>
              </>
            ) : (
              <Link
                href="/login"
                className="py-2 text-base underline underline-offset-4 text-neutral-900"
              >
                Sign in / Sign up
              </Link>
            )}
          </nav>
        </div>
      )}
    </header>
  );
}
