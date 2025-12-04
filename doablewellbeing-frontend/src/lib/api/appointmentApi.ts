import { apiFetch } from "./apiFetch";

export type AppointmentStatus =
  | "REQUESTED"
  | "SCHEDULED"
  | "DECLINED"
  | "CANCELLED"
  | "COMPLETED"
  | "NO_SHOW";

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

export interface BookFromSlotRequest {
  coachId: string;
  slotStart: string;
  durationMinutes: number;
  notes?: string;
}

/* Normal appoinment stuff */
export async function bookAppointmentFromSlot(payload: BookFromSlotRequest) {
  return apiFetch<AppointmentView>("/appointments/slots/book", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

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

/* User related */
export async function getMyAppointments() {
  return apiFetch<AppointmentView[]>("/user/appointment/me");
}


export async function cancelMyAppointment(appointmentId: string) {
  return apiFetch<AppointmentView>(`/user/appointment/${appointmentId}/cancel`, {
    method: "PATCH",
  });
}