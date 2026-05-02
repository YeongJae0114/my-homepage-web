import type { Direction } from "./mapData";

type MobileJoypadProps = {
  onStart: (direction: Direction) => void;
  onStop: () => void;
};

const controls: Array<{ direction: Direction; label: string; className: string }> = [
  { direction: "up", label: "상", className: "col-start-2 row-start-1" },
  { direction: "left", label: "좌", className: "col-start-1 row-start-2" },
  { direction: "down", label: "하", className: "col-start-2 row-start-2" },
  { direction: "right", label: "우", className: "col-start-3 row-start-2" },
];

export function MobileJoypad({ onStart, onStop }: MobileJoypadProps) {
  return (
    <div className="absolute bottom-3 left-1/2 z-30 grid w-36 -translate-x-1/2 touch-none grid-cols-3 grid-rows-2 gap-1.5 sm:w-44 sm:gap-2 md:hidden">
      {controls.map((control) => (
        <button
          key={control.direction}
          type="button"
          aria-label={`${control.label} 이동`}
          className={`${control.className} h-11 rounded-lg border border-white/15 bg-surface-950/70 text-sm font-bold text-zinc-100 shadow-glow backdrop-blur active:bg-emerald-300/25 sm:h-14`}
          onContextMenu={(event) => event.preventDefault()}
          onMouseDown={(event) => {
            event.preventDefault();
            onStart(control.direction);
          }}
          onMouseUp={(event) => {
            event.preventDefault();
            onStop();
          }}
          onMouseLeave={onStop}
          onTouchStart={(event) => {
            event.preventDefault();
            onStart(control.direction);
          }}
          onTouchEnd={(event) => {
            event.preventDefault();
            onStop();
          }}
          onTouchCancel={(event) => {
            event.preventDefault();
            onStop();
          }}
        >
          {control.label}
        </button>
      ))}
    </div>
  );
}
