import { apiFetch } from "./apiFetch";

/* ----------------------- */
/*     Appointment types   */
/* ----------------------- */
// Backend-ben kisbetűs enum van:
// requested, scheduled, declined, cancelled, completed, no_show
export type AppointmentStatus =
  | "requested"
  | "scheduled"
  | "declined"
  | "cancelled"
  | "completed"
  | "no_show";

export interface AppointmentView {
  id: string;
  coachId: string;
  clientId: string;
  coachName?: string;
  clientName?: string;
  startsAt: string;
  endsAt: string;
  status: AppointmentStatus;
  meetingUrl?: string | null;
  confirmedAt?: string | null;
  notes?: string | null;
}

/* ----------------------- */
/*     Request DTO-k       */
/* ----------------------- */
export interface BookFromSlotRequest {
  coachId: string;
  slotStart: string;
  durationMinutes: number;
  notes?: string;
}

export interface InstantBookRequest extends BookFromSlotRequest {}

/* ----------------------- */
/*  Normal Appointment API */
/* ----------------------- */

export async function bookAppointmentFromSlot(payload: BookFromSlotRequest) {
  return apiFetch<AppointmentView>("/appointments/slots/book", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

/* ----------------------- */
/*     INSTANT BOOK API    */
/* ----------------------- */

export async function bookAppointmentFromSlotInstant(
  payload: InstantBookRequest
) {
  return apiFetch<AppointmentView>("/appointments/dev/instant-book", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

/* ----------------------- */
/*   Coach appointment API */
/* ----------------------- */

export async function getCoachAppointments() {
  return apiFetch<AppointmentView[]>("/appointments/coach/me");
}

export async function confirmAppointment(appointmentId: string) {
  return apiFetch<AppointmentView>(`/appointments/${appointmentId}/confirm`, {
    method: "PATCH",
  });
}

export async function declineAppointment(appointmentId: string, reason?: string) {
  return apiFetch<AppointmentView>(`/appointments/${appointmentId}/decline`, {
    method: "PATCH",
    body: JSON.stringify({ reason: reason ?? "" }),
  });
}

/* ----------------------- */
/*     User appointment    */
/* ----------------------- */

export async function getMyAppointments() {
  return apiFetch<AppointmentView[]>("/user/appointment/me");
}

export async function cancelMyAppointment(appointmentId: string) {
  return apiFetch<AppointmentView>(`/user/appointment/${appointmentId}/cancel`, {
    method: "PATCH",
  });
}
