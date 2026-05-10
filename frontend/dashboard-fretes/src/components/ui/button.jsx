import { cn } from "@/lib/utils";

export function Button({ className = "", variant = "default", size = "default", ...props }) {
  const variants = {
    default: "bg-primary text-primary-foreground hover:brightness-95",
    outline: "border border-border bg-transparent text-foreground hover:border-primary",
    ghost: "bg-transparent text-foreground hover:bg-white/5",
  };
  const sizes = {
    default: "h-9 px-4 text-sm",
    icon: "h-9 w-9 p-0",
    sm: "h-8 px-3 text-xs",
  };
  return (
    <button
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-md font-semibold transition disabled:pointer-events-none disabled:opacity-50",
        variants[variant],
        sizes[size],
        className
      )}
      {...props}
    />
  );
}
