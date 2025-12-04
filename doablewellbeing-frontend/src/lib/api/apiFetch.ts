export async function apiFetch<T>(
    path: string,
    options: RequestInit = {}
): Promise<T> {
    const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || "";
    const res = await fetch(`${baseURL}${path}`, {
        ...options,
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
            ...(options.headers || {}),
        },
    });

    if (!res.ok) {
        let text = "";
        try {
            text = await res.text();
        }catch {}
        throw new Error(`API error: ${res.status} ${res.statusText} - ${text || "no details"}`);
    }

    if (res.status === 204) {
        return undefined as T;
    }   
    
    return (await res.json()) as T;  
}