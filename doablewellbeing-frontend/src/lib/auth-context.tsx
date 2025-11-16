// 📄 src/lib/auth-context.tsx
"use client";
import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { api } from "@/lib/api";

type User = {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
};

type AuthCtx = {
  user: User | null;
  loading: boolean;
  refresh: () => Promise<void>;
  signOut: () => Promise<void>;
};

// 🔹 EZ a "Ctx", amiről a hiba beszél
const Ctx = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = async () => {
    try {
      const res = await api.get<User>("/auth/me", { withCredentials: true });
      setUser(res.data);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const signOut = async () => {
    try {
      await api.post("/auth/logout"); // vagy később /auth/logout
    } finally {
      setUser(null);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  // 🔹 Itt van a Provider (a hiba erre vonatkozott)
  return (
    <Ctx.Provider value={{ user, loading, refresh, signOut }}>
      {children}
    </Ctx.Provider>
  );
}


// Helper hook, hogy kényelmes legyen az elérés
export function useAuth() {
  const context = useContext(Ctx);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
