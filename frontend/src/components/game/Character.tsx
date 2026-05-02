import { CHARACTER_SPRITE, MAP_COLUMNS, MAP_ROWS, type Direction, type Position } from "./mapData";

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
  const spriteColumn = Math.min(frame, CHARACTER_SPRITE.columns - 1);
  const spriteRow = CHARACTER_SPRITE.directionRows[direction];
  const backgroundX = CHARACTER_SPRITE.columns === 1 ? 0 : (spriteColumn / (CHARACTER_SPRITE.columns - 1)) * 100;
  const backgroundY = CHARACTER_SPRITE.rows === 1 ? 0 : (spriteRow / (CHARACTER_SPRITE.rows - 1)) * 100;

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
      <div
        aria-hidden="true"
        className="absolute inset-0 bg-no-repeat"
        style={{
          backgroundImage: `image-set(url("${CHARACTER_SPRITE.webp}") type("image/webp"), url("${CHARACTER_SPRITE.png}") type("image/png"))`,
          backgroundSize: `${CHARACTER_SPRITE.columns * 100}% ${CHARACTER_SPRITE.rows * 100}%`,
          backgroundPosition: `${backgroundX}% ${backgroundY}%`,
          imageRendering: "auto",
        }}
      />
    </div>
  );
}
