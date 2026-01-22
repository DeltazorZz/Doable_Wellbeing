"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "../api/api"; 

export function useRequireAuth() {
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let alive = true;

    (async () => {
      try {
        // ez cookie-val megy (withCredentials=true)
        const res = await api.get("/auth/me");

        // ha a te interceptorod "soft-olja" a 401-et, akkor itt res.data lehet null
        if (!res || (res as any).status === 401 || !res.data) {
          router.replace("/login");
          return;
        }

        if (alive) setReady(true);
      } catch {
        // bármi gond → login
        router.replace("/login");
      }
    })();

    return () => {
      alive = false;
    };
  }, [router]);

  return ready; // false amíg ellenőrizzük, true ha auth ok
}
