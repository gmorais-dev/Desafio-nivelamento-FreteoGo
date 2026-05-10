import { cn } from "@/lib/utils";

export function Badge({ className = "", style, ...props }) {
  return (
    <span
      className={cn("inline-flex items-center rounded-md px-2 py-1 text-[10px] font-bold uppercase", className)}
      style={style}
      {...props}
    />
  );
}
