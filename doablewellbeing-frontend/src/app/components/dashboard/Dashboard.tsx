"use client";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Responsive, WidthProvider } from "react-grid-layout";
import {DEFAULT_LAYOUTS, breakpoints, cols, type Layouts, loadDashboard, saveDashboard} from "@/lib/dashboard/persistence";
import { WidgetRegistry, type WidgetType, type WidgetInstance } from "../widgets";
import WidgetChrome from "./WidgetChrome";
import { humanize } from "@/lib/utils";

const ResponsiveGridLayout = WidthProvider(Responsive);

// const ResponsiveRGL = (prpos: any) => {
//     const WRGL = useMemo(() => WidthProvider(ResponsiveGridLayout as any), []);
//     return <WRGL {...prpos} />;
// }

export default function Dashboard() {
    const [layouts, setLayouts] = useState<Layouts>(DEFAULT_LAYOUTS);
    const [widgets, setWidgets] = useState<WidgetInstance[]>([]);
    const [loading, setLoading] = useState(true);
      const [editMode, setEditMode] = useState(false);

    useEffect(() => {
        loadDashboard().then(state => {
            setWidgets(state.widgets);
            setLayouts(state.layouts);
        }).finally(() => setLoading(false));
    }, []);

  useEffect(() => {
    if (loading) return; 
    void saveDashboard(widgets, layouts);
  }, [widgets, layouts, loading]);
  
  const onLayoutChange = useCallback(
    (_current: any, all: Layouts) => {
      setLayouts(all);
  
    },
    []
  );

    const removeWidget = useCallback((id: string) => {
        setWidgets(prev => prev.filter(w => w.id !== id));
        setLayouts(prev => {
            const copy: Layouts = {...prev};
        (Object.keys(copy) as (keyof Layouts) []).forEach(bp => {
                if (!bp || !copy[bp]) return;
                    copy[bp] = copy[bp].filter(item => item.i !== id);
        });
            return copy;
        });
    }, []);
    

function WidgetRenderer({ widget }: { widget: WidgetInstance }) {
 const Comp = WidgetRegistry[widget.type];
 if (!Comp) return null;
  return <Comp widget={widget} />;
}


function addToLayouts(prev: Layouts, id: string): Layouts {
  const next: Layouts = { ...prev };
  (Object.keys(cols) as (keyof typeof cols)[]).forEach(bp => {
    const arr = (next[bp] = [...(next[bp] || [])]);
    const w = Math.max(3, Math.min(4, Math.floor((cols[bp] || 12) / 3)));
    const h = 5;
    const y = arr.length ? Math.max(...arr.map(l => l.y + l.h)) : 0;
    arr.push({ i: id, x: 0, y, w, h });
  });
  return next;
}

    const addWidget = useCallback((type: WidgetType) => {
    const id = `${type}-${(globalThis.crypto?.randomUUID?.() ?? Math.random().toString(36)).slice(0,6)}`;
        setWidgets(prev => [...prev, { id, type, title: humanize(type) }]);
        setLayouts(prev => addToLayouts(prev, id));
    }, []);

     return (
    <main className="mx-auto max-w-[1400px] p-4 md:p-6">
        <Header   editMode={editMode} onToggleEdit={() => setEditMode((prev) => !prev)} />
      <Toolbar onAdd={addWidget}  editMode={editMode} />
      <section className="mt-4">
      <ResponsiveGridLayout
        className="layout"
        layouts={layouts}
        breakpoints={breakpoints}
        cols={cols}
        margin={[12, 12]}
        containerPadding={[0, 0]}
        rowHeight={16}
        compactType="vertical"
        onLayoutChange={onLayoutChange}
        isDraggable={editMode}
        isResizable={editMode}
        draggableHandle={editMode ? ".dw-drag-handle" : undefined}
        draggableCancel=".dw-no-drag"
        isBounded
      >
      {widgets.map(w => (
        <div key={w.id} className="group">
          {editMode ? (
              <WidgetChrome title={w.title || humanize(w.type)} onRemove={() => removeWidget(w.id)}>
              <WidgetRenderer widget={w} />
              </WidgetChrome>
              ) : (
                <WidgetRenderer widget={w} />
              )}
            </div>
                ))}
              </ResponsiveGridLayout>
            </section>
    </main>
  );
}

function Header({ editMode, onToggleEdit }: { editMode: boolean; onToggleEdit: () => void }) {
  return (
    <div className="flex items-center justify-between gap-4 text-black">
      <div>
        <h1 className="text-2xl md:text-3xl font-bold tracking-tight">Your Doable Dashboard</h1>
        <p className="text-sm md:text-base text-muted-foreground/80">
          {editMode
            ? "Drag to move, pull corner to resize. Changes save automatically."
            : "View your wellbeing at a glance. Switch to Edit mode to rearrange widgets."}
        </p>
      </div>
      <div className="flex items-center gap-2">
        <button
          onClick={onToggleEdit}
          className="rounded-full border border-zinc-700/70 bg-zinc-900/80 px-3 py-1.5 text-xs font-medium text-zinc-100 hover:bg-zinc-800 transition"
        >
          {editMode ? "Done editing" : "Edit layout"}
        </button>
      </div>
    </div>
  );
}

function Toolbar({ onAdd, editMode }: { onAdd: (t: WidgetType) => void; editMode: boolean }) {
  if (!editMode) return null; // view módban ne mutassuk

  const options = Object.keys(WidgetRegistry) as WidgetType[];
  return (
    <div className="mt-4 flex flex-wrap items-center gap-2">
      <span className="text-sm text-zinc-400">Add widget:</span>
      {options.map(t => (
        <button
          key={t}
          onClick={() => onAdd(t)}
          className="rounded-2xl border border-zinc-700/70 bg-zinc-900/60 px-3 py-1.5 text-sm hover:bg-zinc-900 transition"
        >
          {humanize(t)}
        </button>
      ))}
    </div>
  );
}


