import { apiFetch } from "@/lib/api/apiFetch";

export type Coach = {
    id: string;
    displayName: string;
    bio?: string;
    expertise?: string;
    timezone?: string;
};

export async function fetchCoaches(): Promise<Coach[]> {
    return await apiFetch<Coach[]>("/api/coaches");
}