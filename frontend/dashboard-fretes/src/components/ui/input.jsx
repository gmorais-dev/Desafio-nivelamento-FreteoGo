import { cn } from "@/lib/utils";

export function Input({ className = "", ...props }) {
  return (
    <input
      className={cn(
        "h-9 w-full rounded-md border border-border bg-background px-3 text-sm text-foreground outline-none transition placeholder:text-muted-foreground focus:border-primary",
        className
      )}
      {...props}
    />
  );
}
