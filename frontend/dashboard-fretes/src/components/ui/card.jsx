import { cn } from "@/lib/utils";

export function Card({ className = "", ...props }) {
  return (
    <section
      className={cn(
        "rounded-lg border border-border bg-card text-card-foreground shadow-sm",
        className
      )}
      {...props}
    />
  );
}

export function CardHeader({ className = "", ...props }) {
  return <div className={cn("p-4 pb-2", className)} {...props} />;
}

export function CardTitle({ className = "", ...props }) {
  return <h3 className={cn("text-sm font-semibold", className)} {...props} />;
}

export function CardContent({ className = "", ...props }) {
  return <div className={cn("p-4 pt-2", className)} {...props} />;
}
