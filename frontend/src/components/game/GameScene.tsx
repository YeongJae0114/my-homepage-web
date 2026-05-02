import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Character } from "./Character";
import { MapSelector } from "./MapSelector";
import { MenuModal } from "./MenuModal";
import { MobileJoypad } from "./MobileJoypad";
import { MAP_COLUMNS, MAP_DATA, MAP_ROWS, type Direction, type MapDefinition, type Position } from "./mapData";

const frameSequence = [0, 1, 0, 2];

function getNextPosition(position: Position, direction: Direction): Position {
  const delta = {
    down: { x: 0, y: 1 },
    up: { x: 0, y: -1 },
    left: { x: -1, y: 0 },
    right: { x: 1, y: 0 },
  }[direction];

  return {
    x: position.x + delta.x,
    y: position.y + delta.y,
  };
}

function canMove(map: MapDefinition, position: Position) {
  return position.y >= 0 && position.y < MAP_ROWS && position.x >= 0 && position.x < MAP_COLUMNS && map.grid[position.y]?.[position.x] === 0;
}

export function GameScene() {
  const navigate = useNavigate();
  const [selectedMapId, setSelectedMapId] = useState(MAP_DATA[0].id);
  const selectedMap = useMemo(() => MAP_DATA.find((map) => map.id === selectedMapId) ?? MAP_DATA[0], [selectedMapId]);
  const [position, setPosition] = useState<Position>({ x: selectedMap.startX, y: selectedMap.startY });
  const [direction, setDirection] = useState<Direction>("down");
  const [isMoving, setIsMoving] = useState(false);
  const [frameIndex, setFrameIndex] = useState(0);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const activeDirection = useRef<Direction | null>(null);

  const move = useCallback(
    (nextDirection: Direction) => {
      setDirection(nextDirection);
      setPosition((current) => {
        const next = getNextPosition(current, nextDirection);

        if (!canMove(selectedMap, next)) {
          return current;
        }

        const zone = selectedMap.zones.find((item) => item.x === next.x && item.y === next.y);

        if (zone) {
          window.setTimeout(() => navigate(zone.path), 160);
        }

        return next;
      });
    },
    [navigate, selectedMap],
  );

  const startMoving = useCallback(
    (nextDirection: Direction) => {
      activeDirection.current = nextDirection;
      setIsMoving(true);
      move(nextDirection);
    },
    [move],
  );

  const stopMoving = useCallback(() => {
    activeDirection.current = null;
    setIsMoving(false);
    setFrameIndex(0);
  }, []);

  useEffect(() => {
    setPosition({ x: selectedMap.startX, y: selectedMap.startY });
    setDirection("down");
    stopMoving();
  }, [selectedMap, stopMoving]);

  useEffect(() => {
    if (!isMoving) {
      return;
    }

    const walkTimer = window.setInterval(() => {
      setFrameIndex((current) => (current + 1) % frameSequence.length);
    }, 150);

    const moveTimer = window.setInterval(() => {
      if (activeDirection.current) {
        move(activeDirection.current);
      }
    }, 200);

    return () => {
      window.clearInterval(walkTimer);
      window.clearInterval(moveTimer);
    };
  }, [isMoving, move]);

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      const keyMap: Partial<Record<string, Direction>> = {
        ArrowDown: "down",
        ArrowUp: "up",
        ArrowLeft: "left",
        ArrowRight: "right",
        s: "down",
        w: "up",
        a: "left",
        d: "right",
      };
      const nextDirection = keyMap[event.key];

      if (!nextDirection || activeDirection.current === nextDirection) {
        return;
      }

      event.preventDefault();
      startMoving(nextDirection);
    };

    const handleKeyUp = () => stopMoving();

    window.addEventListener("keydown", handleKeyDown);
    window.addEventListener("keyup", handleKeyUp);

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      window.removeEventListener("keyup", handleKeyUp);
    };
  }, [startMoving, stopMoving]);

  return (
    <section className="game-home fixed inset-0 overflow-hidden bg-surface-950 text-zinc-50">
      <div className="absolute inset-0 grid place-items-center p-3 sm:p-5">
        <div className="relative aspect-video max-h-[100vh] w-full max-w-[100vw] overflow-hidden rounded-lg border border-white/10 bg-surface-900 shadow-glow">
          <img src={selectedMap.image} alt={selectedMap.name} draggable={false} className="absolute inset-0 h-full w-full select-none object-cover" />
          {selectedMap.zones.map((zone) => (
            <div
              key={zone.id}
              className="absolute z-10 grid -translate-x-1/2 -translate-y-1/2 place-items-center rounded-full border border-emerald-200/45 bg-emerald-300/18 px-3 py-1 text-xs font-semibold text-emerald-50 shadow-[0_0_24px_rgba(110,231,183,.35)] animate-pulse"
              style={{
                left: `${(zone.x + 0.5) * (100 / MAP_COLUMNS)}%`,
                top: `${(zone.y + 0.5) * (100 / MAP_ROWS)}%`,
              }}
            >
              {zone.label}
            </div>
          ))}
          <Character position={position} direction={direction} frame={frameSequence[frameIndex]} />
        </div>
      </div>

      <MapSelector maps={MAP_DATA} selectedMapId={selectedMap.id} onSelect={setSelectedMapId} />
      <button
        type="button"
        className="fixed right-4 top-4 z-40 rounded-lg border border-white/10 bg-surface-950/75 px-4 py-2 text-sm font-semibold text-zinc-100 shadow-glow backdrop-blur transition hover:bg-white/[0.08]"
        onClick={() => setIsMenuOpen(true)}
      >
        일반 메뉴로 보기
      </button>
      <div className="fixed bottom-5 left-5 z-30 hidden rounded-lg border border-white/10 bg-surface-950/65 px-3 py-2 font-mono text-xs text-zinc-400 backdrop-blur md:block">
        Arrow / WASD로 이동
      </div>
      <MobileJoypad onStart={startMoving} onStop={stopMoving} />
      <MenuModal isOpen={isMenuOpen} onClose={() => setIsMenuOpen(false)} />
    </section>
  );
}
