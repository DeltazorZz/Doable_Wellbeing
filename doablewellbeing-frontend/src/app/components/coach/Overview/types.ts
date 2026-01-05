import { AppointmentStatus } from "@/lib/api/appointmentApi";

export type ClientSummaryView = {
  id: string;
  name: string;
  email: string;
};

export type CoachCalendarEventView = {
  id: string;
  title: string;
  startsAt: string; // ISO
  endsAt: string;   // ISO
  status: AppointmentStatus;
  meetingUrl?: string | null;
  externalCalendarId?: string | null;
  client: ClientSummaryView;
  notesPreview?: string | null;
};