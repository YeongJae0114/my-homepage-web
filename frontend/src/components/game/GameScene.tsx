import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Character } from "./Character";
import { MapSelector } from "./MapSelector";
import { MenuModal } from "./MenuModal";
import { MobileJoypad } from "./MobileJoypad";
import { MAP_COLUMNS, MAP_DATA, MAP_ROWS, characterFrames, type Direction, type MapDefinition, type Position } from "./mapData";

const MOVE_STEP = 0.25;
const VERTICAL_MOVE_STEP = MOVE_STEP * (MAP_ROWS / MAP_COLUMNS);
const MOVE_INTERVAL_MS = 58;
const WALK_FRAME_INTERVAL_MS = 95;
const IDLE_FRAME_DELAY_MS = 170;
const ZONE_TRIGGER_RADIUS = 0.28;
const frameSequence = [1, 0, 2, 0];

function getNextPosition(position: Position, direction: Direction): Position {
  const delta = {
    down: { x: 0, y: VERTICAL_MOVE_STEP },
    up: { x: 0, y: -VERTICAL_MOVE_STEP },
    left: { x: -MOVE_STEP, y: 0 },
    right: { x: MOVE_STEP, y: 0 },
  }[direction];

  return {
    x: Number((position.x + delta.x).toFixed(3)),
    y: Number((position.y + delta.y).toFixed(3)),
  };
}

function canMove(map: MapDefinition, position: Position) {
  const gridX = Math.round(position.x);
  const gridY = Math.round(position.y);

  return gridY >= 0 && gridY < MAP_ROWS && gridX >= 0 && gridX < MAP_COLUMNS && map.grid[gridY]?.[gridX] === 0;
}

function findActiveZone(map: MapDefinition, position: Position) {
  return map.zones.find((zone) => Math.hypot(zone.x - position.x, zone.y - position.y) <= ZONE_TRIGGER_RADIUS);
}

export function GameScene() {
  const navigate = useNavigate();
  const sceneRef = useRef<HTMLDivElement | null>(null);
  const [selectedMapId, setSelectedMapId] = useState(MAP_DATA[0].id);
  const selectedMap = useMemo(() => MAP_DATA.find((map) => map.id === selectedMapId) ?? MAP_DATA[0], [selectedMapId]);
  const [position, setPosition] = useState<Position>({ x: selectedMap.startX, y: selectedMap.startY });
  const [direction, setDirection] = useState<Direction>("down");
  const [isMoving, setIsMoving] = useState(false);
  const [frameIndex, setFrameIndex] = useState(0);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [sceneSize, setSceneSize] = useState({ width: 0, height: 0 });
  const activeDirection = useRef<Direction | null>(null);
  const activeZoneId = useRef<string | null>(null);
  const idleFrameTimer = useRef<number | null>(null);

  const clearIdleFrameTimer = useCallback(() => {
    if (idleFrameTimer.current) {
      window.clearTimeout(idleFrameTimer.current);
      idleFrameTimer.current = null;
    }
  }, []);

  useEffect(() => {
    const scene = sceneRef.current;

    if (!scene) {
      return;
    }

    const updateSceneSize = () => {
      const rect = scene.getBoundingClientRect();
      setSceneSize({
        width: Math.round(rect.width),
        height: Math.round(rect.height),
      });
    };

    updateSceneSize();

    const observer = new ResizeObserver(updateSceneSize);
    observer.observe(scene);

    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    Object.values(characterFrames)
      .flat()
      .forEach((src) => {
        const image = new Image();
        image.src = src;
      });
  }, []);

  useEffect(() => clearIdleFrameTimer, [clearIdleFrameTimer]);

  const move = useCallback(
    (nextDirection: Direction) => {
      setDirection(nextDirection);
      setPosition((current) => {
        const next = getNextPosition(current, nextDirection);

        if (!canMove(selectedMap, next)) {
          return current;
        }

        const zone = findActiveZone(selectedMap, next);

        if (zone && activeZoneId.current !== zone.id) {
          activeZoneId.current = zone.id;
          window.setTimeout(() => navigate(zone.path), 160);
        }

        if (!zone) {
          activeZoneId.current = null;
        }

        return next;
      });
    },
    [navigate, selectedMap],
  );

  const startMoving = useCallback(
    (nextDirection: Direction) => {
      clearIdleFrameTimer();
      activeDirection.current = nextDirection;
      setFrameIndex(0);
      setIsMoving(true);
      move(nextDirection);
    },
    [clearIdleFrameTimer, move],
  );

  const stopMoving = useCallback(() => {
    activeDirection.current = null;
    setIsMoving(false);
    clearIdleFrameTimer();
    idleFrameTimer.current = window.setTimeout(() => {
      setFrameIndex(0);
      idleFrameTimer.current = null;
    }, IDLE_FRAME_DELAY_MS);
  }, [clearIdleFrameTimer]);

  useEffect(() => {
    setPosition({ x: selectedMap.startX, y: selectedMap.startY });
    setDirection("down");
    activeZoneId.current = null;
    stopMoving();
  }, [selectedMap, stopMoving]);

  useEffect(() => {
    if (!isMoving) {
      return;
    }

    const walkTimer = window.setInterval(() => {
      setFrameIndex((current) => (current + 1) % frameSequence.length);
    }, WALK_FRAME_INTERVAL_MS);

    const moveTimer = window.setInterval(() => {
      if (activeDirection.current) {
        move(activeDirection.current);
      }
    }, MOVE_INTERVAL_MS);

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
        <div
          ref={sceneRef}
          className="relative aspect-square w-full max-w-[min(78vmin,680px)] overflow-hidden rounded-lg border border-white/10 bg-surface-900 shadow-glow sm:max-w-[min(74vmin,700px)]"
        >
          <img src={selectedMap.image} alt={selectedMap.name} draggable={false} className="absolute inset-0 h-full w-full select-none object-contain" />
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
          {sceneSize.width > 0 && <Character position={position} direction={direction} frame={frameSequence[frameIndex]} sceneSize={sceneSize} />}
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
