
export type DayCell = {

  iso: string;

  day: number;

  inMonth: boolean;
};

export type AvailabilityFormState = {
  startTime: string;          
  endTime: string;           
  recurring: boolean;
  repeatWeeks?: number;       
};


export type AvailabilityDaySummary = {
  date: string;               
  hasAvailability: boolean;
  totalWindows?: number;
};
