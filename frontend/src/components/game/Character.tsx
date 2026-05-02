import { characterFrames, MAP_COLUMNS, MAP_ROWS, type Direction, type Position } from "./mapData";

type CharacterProps = {
  position: Position;
  direction: Direction;
  frame: number;
  sceneSize: {
    width: number;
    height: number;
  };
};

function roundToEven(value: number) {
  return Math.max(2, Math.round(value / 2) * 2);
}

export function Character({ position, direction, frame, sceneSize }: CharacterProps) {
  const cellWidth = sceneSize.width / MAP_COLUMNS;
  const cellHeight = sceneSize.height / MAP_ROWS;
  const width = roundToEven(cellWidth * 1.25);
  const height = roundToEven(width * 1.68);
  const x = Math.round((position.x + 0.5) * cellWidth - width / 2);
  const y = Math.round((position.y + 0.5) * cellHeight - height * 0.7);
  const activeFrameSrc = characterFrames[direction][frame] ?? characterFrames[direction][0];

  return (
    <div
      aria-label="Player character"
      className="pointer-events-none absolute left-0 top-0 z-20 select-none transition-transform duration-75 ease-linear will-change-transform"
      role="img"
      style={{
        width,
        height,
        transform: `translate3d(${x}px, ${y}px, 0)`,
      }}
    >
      <img
        src={activeFrameSrc}
        alt=""
        aria-hidden="true"
        decoding="async"
        draggable={false}
        className="absolute inset-0 h-full w-full object-contain"
        style={{ imageRendering: "auto" }}
      />
    </div>
  );
}
