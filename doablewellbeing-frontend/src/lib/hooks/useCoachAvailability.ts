
"use client";

import { useCallback, useEffect, useState } from "react";
import {
  CoachAvailabilityRequest,
  CoachAvailabilityResponse,
  getCoachAvailabilitiesForDate,
  createCoachAvailability,
  deleteMyCoachAvailability,
} from "@/lib/api/coachAvailabilityApi";

const todayISODate = () => new Date().toISOString().slice(0, 10);

export function useCoachAvailability(initialDate: string = todayISODate()) {
 
  const [selectedDate, setSelectedDate] = useState<string>(initialDate);

  const [availabilities, setAvailabilities] = useState<
    CoachAvailabilityResponse[]
  >([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [form, setForm] = useState<CoachAvailabilityRequest>({
    date: initialDate,
    startTime: "09:00",
    endTime: "12:00",
    recurring: false,
  });

  const loadAvailabilities = useCallback(async (date: string) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getCoachAvailabilitiesForDate(date);
      setAvailabilities(data);
    } catch (e: any) {
      setError(e.message ?? "Failed to load availabilities");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAvailabilities(selectedDate);
    setForm((prev) => ({ ...prev, date: selectedDate }));
  }, [selectedDate, loadAvailabilities]);

  const createAvailability = async () => {
    setError(null);
    setSuccess(null);

    if (form.endTime <= form.startTime) {
      setError("End time must be after start time.");
      return;
    }

    try {
      await createCoachAvailability(form);
      setSuccess("Availability added.");
      await loadAvailabilities(selectedDate);
    } catch (e: any) {
      setError(e.message ?? "Failed to create availability");
    }
  };

  const removeAvailability = async (id: string) => {
    setError(null);
    setSuccess(null);
    try {
      await deleteMyCoachAvailability(id);
      setSuccess("Availability removed.");
      await loadAvailabilities(selectedDate);
    } catch (e: any) {
      setError(e.message ?? "Failed to delete availability");
    }
  };

  return {
    selectedDate,
    setSelectedDate,
    availabilities,
    loading,
    error,
    success,
    form,
    setForm,
    createAvailability,
    removeAvailability,
  };
}
