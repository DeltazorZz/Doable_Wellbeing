import { apiFetch } from "@/lib/api/apiFetch";

export type Coach = {
    id: string;
    displayName: string;
    bio?: string;
    expertise?: string;
    timezone?: string;
};

export async function fetchCoaches(): Promise<Coach[]> {
    const result = await apiFetch<Coach[]>("/api/coaches");
    if (!result.ok) {
        throw new Error(result.errorText || "Failed to fetch coaches");
    }
    return result.data;
}