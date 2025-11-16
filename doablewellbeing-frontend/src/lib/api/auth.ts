import { request } from "./client";

export interface LoginDto{
    email: string;
    password: string;
}
export interface AuthUser{
    id: string;
    email: string;
    firstName: string;
    lastName: string;
}
export function login(dto: LoginDto){
    return request<AuthUser, LoginDto>({
        method: "POST",
        url: "/auth/login",
        body: dto,
    });
}
export function logout(){
    return request<void>({
        method: "POST",
        url: "/auth/logout",
    });
}
export function getCurrentUser(){
    return request<AuthUser>({
        method: "GET",
        url: "/auth/me",
    });
}