    
export function humanize(k: string): string {
      return k
        .replace(/([a-z])([A-Z])/g, "$1 $2")
        .replace(/[-_]/g, " ")
        .replace(/^\w/, c => c.toUpperCase());
    }
