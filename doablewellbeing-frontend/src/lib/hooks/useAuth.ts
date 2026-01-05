import { useState } from "react";
import { useRouter } from "next/navigation";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { api } from "@/lib/api/api";
import { initCsrfToken } from "@/app/components/security/CsrfBootstrap";

const loginSchema = z.object({
  email: z.string().email("Please enter a valid email"),
  password: z.string().min(6, "At least 6 characters"),
  remember: z.boolean().optional(),
});

const registerSchema = loginSchema
  .extend({
    firstName: z.string().min(1, "First name is required"),
    lastName: z.string().min(1, "Last name is required"),
    confirmPassword: z.string(),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });

type LoginValues = z.infer<typeof loginSchema>;
type RegisterValues = z.infer<typeof registerSchema>;

export function useAuthForm(mode: "login" | "register" | "CoachReg") {
  const router = useRouter();
  const [showPassword, setShowPassword] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const isLogin = mode === "login";
  const isRegister = mode === "register";

  const form = useForm<LoginValues | RegisterValues>({
    resolver: zodResolver(isLogin ? loginSchema : registerSchema),
    defaultValues: isLogin
      ? { email: "", password: "", remember: true }
      : { email: "", password: "", firstName: "", lastName: "" },
    mode: "onSubmit",
  });

  async function onSubmit(values: LoginValues | RegisterValues) {
    setServerError(null);
    setLoading(true);
try {
      if (isLogin) {
        const { email, password, remember } = values as LoginValues;
        await api.post("/auth/login", { email, password, remember });

     
        try {
          await initCsrfToken();
        } catch (e) {
          console.warn("CSRF re-init after login failed", e);
        }
      } else if (isRegister) {
        const { email, password, firstName, lastName } = values as RegisterValues;
        await api.post("/auth/register", { email, password, firstName, lastName });
        await api.post("/auth/login", { email, password });

        try {
          await initCsrfToken();
        } catch (e) {
          console.warn("CSRF re-init after register/login failed", e);
        }
      } else {
        const { email, password, firstName, lastName } = values as RegisterValues;
        await api.post("/auth/registerCoach", { email, password, firstName, lastName });
        await api.post("/auth/login", { email, password });

        try {
          await initCsrfToken();
        } catch (e) {
          console.warn("CSRF re-init after coach-register/login failed", e);
        }
      }
      router.replace("/");
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || "Something went wrong";
      setServerError(msg);
    } finally {
      setLoading(false);
    }
  }

  return {
    form,
    showPassword,
    setShowPassword,
    loading,
    serverError,
    onSubmit,
    isLogin,
    isRegister,
  };
}