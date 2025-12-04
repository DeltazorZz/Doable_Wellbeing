import axios, {AxiosError, AxiosInstance, InternalAxiosRequestConfig} from "axios";


export const api: AxiosInstance = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json",        
    },
});

function readCookie(name: string): string | null {
    if (typeof document === "undefined") return null;
    const match = document.cookie.match(new RegExp(`(?:^|; )${name.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&")}=([^;]*)`));
    return match ? decodeURIComponent(match[1]) : null;
}

api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const xsrf = readCookie("XSRF-TOKEN");
        if (xsrf) {
            (config.headers as any) ["X-XSRF-TOKEN"] = xsrf;
    }
    (config.headers as any)["X-Requested-With"] = "XMLHttpRequest";
    return config;
    },
    (error) => Promise.reject(error)
);

interface RetriableRequestConfig extends InternalAxiosRequestConfig {
    _retry?: boolean;
}

api.interceptors.response.use(
  (res) => res,
  async (err: AxiosError) => {
    const status = err.response?.status;
    const original = err.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;


    if (original?.url?.includes("/auth/refresh")) {
      return Promise.reject(err);
    }

    if (status === 401 && original && !original._retry) {
      original._retry = true;

      try {
        await api.post("/auth/refresh");
        return api.request(original);
      } catch (refreshErr) {
        return Promise.reject(refreshErr);
      }
    }

    
    return Promise.reject(err);
  }
);