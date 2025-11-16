import type { AxiosRequestConfig, Method } from "axios";
import { api } from "./axios";
export type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

interface RequestOptions <Tbody= unknown>{
    method: HttpMethod;
    url: string;
    body?: Tbody;
    config?: AxiosRequestConfig;
}

export async function request<Tresponse= unknown, Tbody= unknown>(
    options: RequestOptions<Tbody>
): Promise<Tresponse> {
    const { method, url, body, config } = options;
const res = await api.request<Tresponse>({
    method: method as Method,
    url,
    data: body,
    ...config,
});
return res.data;    
}
