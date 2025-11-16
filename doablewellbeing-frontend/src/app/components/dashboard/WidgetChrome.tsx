
export default function WidgetChrome({
  title,
  onRemove,
  children,
}: {
  title: string;
  onRemove: () => void;
  children: React.ReactNode;
}) {
  return (
    <div className="h-full w-full rounded-2xl bg-zinc-950/90 border border-zinc-800/80 flex flex-col overflow-hidden">
      <div className="dw-drag-handle flex items-center justify-between gap-2 px-3 py-1.5 text-xs bg-zinc-900/80 border-b border-zinc-800/80">
        <span className="cursor-move text-zinc-500">⋮⋮</span>
        <span className="flex-1 truncate text-zinc-200">{title}</span>
        <button
          type="button"
          className="dw-no-drag text-[11px] text-red-400 hover:text-red-300"
          onClick={onRemove}
        >
          Remove
        </button>
      </div>
      <div className="flex-1">
        {children}
      </div>
    </div>
  );
}
