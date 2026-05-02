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
      src={image}
      alt="Player character"
      draggable={false}
      className="pointer-events-none absolute z-20 w-[7%] select-none transition-all duration-200 ease-linear"
      style={{
        left: `${(position.x + 0.5) * (100 / MAP_COLUMNS)}%`,
        top: `${(position.y + 0.5) * (100 / MAP_ROWS)}%`,
        transform: "translate(-50%, -70%)",
      }}
    />
  );
}
