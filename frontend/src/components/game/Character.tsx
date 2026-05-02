import { characterFrames, MAP_COLUMNS, MAP_ROWS, type Direction, type Position } from "./mapData";

type CharacterProps = {
  position: Position;
  direction: Direction;
  frame: number;
};

export function Character({ position, direction, frame }: CharacterProps) {
  const image = characterFrames[direction][frame];

  return (
    <img
      key={image}
      src={image}
      alt="Player character"
      decoding="async"
      draggable={false}
      className="pointer-events-none absolute z-20 h-[18%] w-[10%] select-none object-contain transition-[left,top] duration-75 ease-linear"
      style={{
        left: `${(position.x + 0.5) * (100 / MAP_COLUMNS)}%`,
        top: `${(position.y + 0.5) * (100 / MAP_ROWS)}%`,
        transform: "translate(-50%, -70%)",
        imageRendering: "pixelated",
        filter: "drop-shadow(0 8px 10px rgba(0, 0, 0, 0.38))",
      }}
    />
  );
}
