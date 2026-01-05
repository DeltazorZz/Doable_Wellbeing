import { apiFetch } from "./apiFetch";

export interface CoachAvailabilityRequest {
  date: string;
  startTime: string;
  endTime: string;
  recurring?: boolean;
  repeatWeeks?: number;
}

export interface CoachAvailabilityResponse {
  id: string;
  date: string;         
  startTime: string;   
  endTime: string;
  recurring: boolean;
  seriesId: string | null;
  active: boolean;
}


export interface SlotView {
  id: string;
  startsAt: string;
  endsAt: string;
}

const subpath = "/api/coach/availabilities";

export async function getCoachAvailabilitiesForDate(
  date: string
): Promise<CoachAvailabilityResponse[]> {

  const params = new URLSearchParams({
    from: date,
    to: date,
  }).toString();

  return apiFetch<CoachAvailabilityResponse[]>(
    `/api/coach/availabilities/me?${params}`
  );
}


export async function createCoachAvailability(
  payload: CoachAvailabilityRequest
) {
  return apiFetch<CoachAvailabilityResponse[]>(`${subpath}/me`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}


export async function getMyCoachAvailabilities(from: string, to: string) {
  const params = new URLSearchParams({ from, to }).toString();

  return apiFetch<CoachAvailabilityResponse[]>(
    `${subpath}/me?${params}`
  );
}


export async function setDayOff(availabilityId: string) {
  return apiFetch<CoachAvailabilityResponse>(
    `${subpath}/${availabilityId}/day-off`,
    {
      method: "PUT",
    }
  );
}

export async function deleteMyCoachAvailability(availabilityId: string) {
  return apiFetch<void>(`${subpath}/me/${availabilityId}`, {
    method: "DELETE",
  });
}


export async function getCoachSlots(
  coachId: string,
  from: string,
  to: string,
  slotLengthMinutes = 60
) {
  const params = new URLSearchParams({
    from,
    to,
    slotLengthMinutes: String(slotLengthMinutes),
  }).toString();

  return apiFetch<SlotView[]>(
    `${subpath}/${coachId}/slots?${params}`
  );
}
