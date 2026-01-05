"use client";
import { useState } from "react";
import { useForm, FieldErrors } from "react-hook-form";
import { useAuthForm } from "@/lib/hooks/useAuth";

export default function AuthForm({ mode }: { mode: "login" | "register" | "CoachReg" }) {
  const { form, showPassword, setShowPassword, loading, serverError, onSubmit, isLogin, isRegister } =
    useAuthForm(mode);

  const regErrors = form.formState.errors as FieldErrors<any>;
  const loginErrors = form.formState.errors as FieldErrors<any>;

  const baseInput =
    "w-full rounded-xl border px-3 py-2 focus:outline-none focus:ring-2 transition disabled:opacity-60 disabled:cursor-not-allowed";
  const ringOk = "focus:ring-indigo-500";
  const ringErr = "border-red-500 focus:ring-red-500";

  return (
    <div className="w-full max-w-md mx-auto">
      <div className="flex flex-col items-center gap-5 mb-6">
        <img src="/DoableLogo-noBg.png" alt="Doable Wellbeing" className="h-auto w-auto" loading="eager" />
        <div className="text-center">
          <h1 className="text-2xl md:text-3xl font-semibold tracking-tight">
            {isLogin ? "Log in to your account" : "Create your account"}
          </h1>
          <p className="text-sm text-neutral-600 dark:text-neutral-400 mt-1.5">
            {isLogin ? "Welcome back. Please enter your details." : "Join Doable Wellbeing in minutes."}
          </p>
        </div>
      </div>

      <div className="rounded-2xl   bg-white">
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <fieldset disabled={loading} className="space-y-4">
            {!isLogin && (
              <>
                <div>
                  <label htmlFor="firstName" className="block text-sm mb-1 font-medium">
                    First name
                  </label>
                  <input
                    id="firstName"
                    type="text"
                    {...form.register("firstName" as const)}
                    className={`${baseInput} ${regErrors.firstName ? ringErr : "border-neutral-300 " + ringOk}`}
                    placeholder="Chris"
                    aria-invalid={!!regErrors.firstName}
                    aria-describedby={regErrors.firstName ? "firstName-error" : undefined}
                  />
                  {regErrors.firstName && (
                    <p id="firstName-error" className="text-sm text-red-600 mt-1">
                      {regErrors.firstName.message as string}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="lastName" className="block text-sm mb-1 font-medium">
                    Last name
                  </label>
                  <input
                    id="lastName"
                    type="text"
                    {...form.register("lastName" as const)}
                    className={`${baseInput} ${regErrors.lastName ? ringErr : "border-neutral-300 " + ringOk}`}
                    placeholder="Smith"
                    aria-invalid={!!regErrors.lastName}
                    aria-describedby={regErrors.lastName ? "lastName-error" : undefined}
                  />
                  {regErrors.lastName && (
                    <p id="lastName-error" className="text-sm text-red-600 mt-1">
                      {regErrors.lastName.message as string}
                    </p>
                  )}
                </div>
              </>
            )}

            <div>
              <label htmlFor="email" className="block text-sm mb-1 font-medium">
                Email
              </label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                {...form.register("email" as const)}
                className={`${baseInput} ${form.formState.errors.email ? ringErr : "border-neutral-300 " + ringOk}`}
                placeholder="you@example.com"
                aria-invalid={!!form.formState.errors.email}
                aria-describedby={form.formState.errors.email ? "email-error" : undefined}
              />
              {form.formState.errors.email && (
                <p id="email-error" className="text-sm text-red-600 mt-1">
                  {form.formState.errors.email.message as string}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="password" className="block text-sm mb-1 font-medium">
                Password
              </label>
              <div className="relative">
                <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  autoComplete={isLogin ? "current-password" : "new-password"}
                  {...form.register("password" as const)}
                  className={`${baseInput} pr-12 ${form.formState.errors.password ? ringErr : "border-neutral-300 " + ringOk}`}
                  placeholder="••••••••"
                  aria-invalid={!!form.formState.errors.password}
                  aria-describedby={form.formState.errors.password ? "password-error" : undefined}
                />
              </div>
              {form.formState.errors.password && (
                <p id="password-error" className="text-sm text-red-600 mt-1">
                  {form.formState.errors.password.message as string}
                </p>
              )}
            </div>

            {!isLogin && (
              <div>
                <label htmlFor="confirmPassword" className="block text-sm mb-1 font-medium">
                  Confirm password
                </label>
                <input
                  id="confirmPassword"
                  type={showPassword ? "text" : "password"}
                  autoComplete="new-password"
                  {...form.register("confirmPassword" as const)}
                  className={`${baseInput} ${regErrors.confirmPassword ? ringErr : "border-neutral-300 " + ringOk}`}
                  placeholder="Repeat your password"
                  aria-invalid={!!regErrors.confirmPassword}
                  aria-describedby={regErrors.confirmPassword ? "confirmPassword-error" : undefined}
                />
                {regErrors.confirmPassword && (
                  <p id="confirmPassword-error" className="text-sm text-red-600 mt-1">
                    {regErrors.confirmPassword.message as string}
                  </p>
                )}
              </div>
            )}

            <div className="flex items-center justify-between text-sm">
              <label className="inline-flex items-center gap-2 select-none">
                <input type="checkbox" className="size-4 rounded border-neutral-300" {...form.register("remember" as const)} />
                <span>Remember for 30 days</span>
              </label>
              <a href="/forgot-password" className="text-indigo-600 hover:underline">
                Forgot password
              </a>
            </div>

            {serverError && (
              <div role="alert" className="rounded-xl border border-red-200/80 bg-red-50 px-3 py-2 text-sm text-red-700">
                {serverError}
              </div>
            )}

            <button type="submit" disabled={loading} className="w-full rounded-xl py-2.5 font-medium shadow-sm disabled:opacity-60 bg-indigo-600 text-white hover:bg-indigo-700 transition">
              {loading ? (isLogin ? "Signing in…" : "Creating account…") : isLogin ? "Sign in" : "Create account"}
            </button>
          </fieldset>
        </form>

        <div className="mt-6 text-center text-sm">
          {isLogin ? (
            <p>
              Don’t have an account?{" "}
              <a className="text-indigo-600 underline" href="/register">
                Sign up
              </a>
            </p>
          ) : (
            <p>
              Already have an account?{" "}
              <a className="text-indigo-600 underline" href="/login">
                Sign in
              </a>
            </p>
          )}
        </div>
      </div>
    </div>
  );
}