import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState, type KeyboardEvent } from "react";
import { useNavigate } from "react-router-dom";
import { Character } from "./Character";
import { MapSelector } from "./MapSelector";
import { MobileJoypad } from "./MobileJoypad";
import { MAP_COLUMNS, MAP_DATA, MAP_ROWS, characterFrames, type Direction, type MapDefinition, type Position } from "./mapData";

const MOVE_STEP = 0.25;
const VERTICAL_MOVE_STEP = MOVE_STEP * (MAP_ROWS / MAP_COLUMNS);
const MOVE_INTERVAL_MS = 58;
const WALK_FRAME_INTERVAL_MS = 95;
const IDLE_FRAME_DELAY_MS = 170;
const ZONE_TRIGGER_RADIUS = 0.28;
const IDLE_FRAME = 0;
const DEFAULT_SCENE_SIZE = { width: 600, height: 600 };
const MIN_LOADING_MS = 700;
const READY_REVEAL_DELAY_MS = 180;
const LOADING_PROGRESS_INTERVAL_MS = 45;
const walkFrameSequence = [1, 0, 2, 0];

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

function getStartPosition(map: MapDefinition): Position {
  return { x: map.startX, y: map.startY };
}

function getGameAssetSources() {
  return [...MAP_DATA.map((map) => map.image), ...Object.values(characterFrames).flat()];
}

async function preloadImage(src: string) {
  const image = new Image();
  image.decoding = "async";
  image.src = src;

  if (image.decode) {
    await image.decode();
    return;
  }

  await new Promise<void>((resolve, reject) => {
    image.onload = () => resolve();
    image.onerror = () => reject(new Error(`Failed to load image: ${src}`));
  });
}

export function GameScene() {
  const navigate = useNavigate();
  const gameRootRef = useRef<HTMLElement | null>(null);
  const sceneRef = useRef<HTMLDivElement | null>(null);
  const [selectedMapId, setSelectedMapId] = useState(MAP_DATA[0].id);
  const selectedMap = useMemo(() => MAP_DATA.find((map) => map.id === selectedMapId) ?? MAP_DATA[0], [selectedMapId]);
  const [position, setPosition] = useState<Position>(() => getStartPosition(selectedMap));
  const [direction, setDirection] = useState<Direction>("down");
  const [isMoving, setIsMoving] = useState(false);
  const [frame, setFrame] = useState(IDLE_FRAME);
  const [isGameReady, setIsGameReady] = useState(false);
  const [loadingProgress, setLoadingProgress] = useState(0);
  const [sceneSize, setSceneSize] = useState(DEFAULT_SCENE_SIZE);
  const activeDirection = useRef<Direction | null>(null);
  const activeZoneId = useRef<string | null>(null);
  const idleFrameTimer = useRef<number | null>(null);
  const walkFrameIndex = useRef(0);

  const clearIdleFrameTimer = useCallback(() => {
    if (idleFrameTimer.current) {
      window.clearTimeout(idleFrameTimer.current);
      idleFrameTimer.current = null;
    }
  }, []);

  const resetForMap = useCallback(
    (map: MapDefinition) => {
      clearIdleFrameTimer();
      activeDirection.current = null;
      activeZoneId.current = null;
      walkFrameIndex.current = 0;
      setIsMoving(false);
      setFrame(IDLE_FRAME);
      setDirection("down");
      setPosition(getStartPosition(map));
    },
    [clearIdleFrameTimer],
  );

  useLayoutEffect(() => {
    const scene = sceneRef.current;

    if (!scene) {
      return;
    }

    const updateSceneSize = () => {
      const rect = scene.getBoundingClientRect();

      if (rect.width === 0 || rect.height === 0) {
        return;
      }

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
    let isActive = true;
    let targetProgress = 8;
    const startedAt = window.performance.now();
    const sources = getGameAssetSources();
    let loadedCount = 0;

    setLoadingProgress(targetProgress);

    const progressTimer = window.setInterval(() => {
      setLoadingProgress((current) => {
        const cappedTarget = Math.min(targetProgress, 96);

        if (current >= cappedTarget) {
          return current;
        }

        return Math.min(cappedTarget, current + Math.max(1, Math.ceil((cappedTarget - current) * 0.28)));
      });
    }, LOADING_PROGRESS_INTERVAL_MS);

    const markLoaded = () => {
      loadedCount += 1;
      if (isActive) {
        targetProgress = Math.max(targetProgress, Math.round((loadedCount / sources.length) * 96));
      }
    };

    Promise.all(
      sources.map((src) =>
        preloadImage(src)
          .catch(() => undefined)
          .finally(markLoaded),
      ),
    ).then(() => {
      const elapsed = window.performance.now() - startedAt;
      const remaining = Math.max(0, MIN_LOADING_MS - elapsed);

      window.setTimeout(() => {
        if (isActive) {
          window.clearInterval(progressTimer);
          setLoadingProgress(100);
          resetForMap(MAP_DATA[0]);
          window.setTimeout(() => {
            if (isActive) {
              setIsGameReady(true);
            }
          }, READY_REVEAL_DELAY_MS);
        }
      }, remaining);
    });

    return () => {
      isActive = false;
      window.clearInterval(progressTimer);
    };
  }, [resetForMap]);

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
      walkFrameIndex.current = 0;
      setFrame(walkFrameSequence[walkFrameIndex.current]);
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
      walkFrameIndex.current = 0;
      setFrame(IDLE_FRAME);
      idleFrameTimer.current = null;
    }, IDLE_FRAME_DELAY_MS);
  }, [clearIdleFrameTimer]);

  useEffect(() => {
    resetForMap(selectedMap);
  }, [selectedMap, resetForMap]);

  const handleMapSelect = useCallback(
    (mapId: string) => {
      const nextMap = MAP_DATA.find((map) => map.id === mapId);

      if (!nextMap || nextMap.id === selectedMapId) {
        return;
      }

      resetForMap(nextMap);
      setSelectedMapId(nextMap.id);
    },
    [resetForMap, selectedMapId],
  );

  useEffect(() => {
    if (!isMoving) {
      return;
    }

    const walkTimer = window.setInterval(() => {
      walkFrameIndex.current = (walkFrameIndex.current + 1) % walkFrameSequence.length;
      setFrame(walkFrameSequence[walkFrameIndex.current]);
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

  const handleKeyDown = useCallback(
    (event: KeyboardEvent<HTMLElement>) => {
      if (!isGameReady) {
        return;
      }

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

      if (event.key === " ") {
        event.preventDefault();
        return;
      }

      if (!nextDirection || activeDirection.current === nextDirection) {
        return;
      }

      event.preventDefault();
      startMoving(nextDirection);
    },
    [isGameReady, startMoving],
  );

  const handleKeyUp = useCallback(
    (event: KeyboardEvent<HTMLElement>) => {
      if (["ArrowDown", "ArrowUp", "ArrowLeft", "ArrowRight", "s", "w", "a", "d", " "].includes(event.key)) {
        event.preventDefault();
        stopMoving();
      }
    },
    [stopMoving],
  );

  return (
    <section
      ref={gameRootRef}
      className="game-home relative h-full min-h-[320px] overflow-hidden bg-surface-950 text-zinc-50 outline-none"
      tabIndex={0}
      onClick={() => gameRootRef.current?.focus()}
      onKeyDown={handleKeyDown}
      onKeyUp={handleKeyUp}
    >
      {!isGameReady && (
        <div className="absolute inset-0 z-40 grid place-items-center bg-surface-950 text-zinc-50">
          <div className="w-[min(82vw,360px)] rounded-lg border border-white/10 bg-surface-900/80 p-4 shadow-glow backdrop-blur sm:p-5">
            <p className="font-mono text-xs uppercase tracking-[0.2em] text-emerald-300">Loading game assets</p>
            <div className="mt-4 h-2 overflow-hidden rounded-full bg-white/10">
              <div className="h-full rounded-full bg-emerald-300 transition-[width] duration-200 ease-out" style={{ width: `${loadingProgress}%` }} />
            </div>
            <p className="mt-3 text-sm text-zinc-400">캐릭터와 맵을 준비하는 중입니다. {loadingProgress}%</p>
          </div>
        </div>
      )}

      <div className={`absolute inset-0 grid place-items-center p-3 transition-opacity duration-200 sm:p-4 ${isGameReady ? "opacity-100" : "opacity-0"}`}>
        <div
          ref={sceneRef}
          className="relative aspect-square h-[min(82%,520px)] max-h-[calc(100%-1rem)] max-w-[calc(100%-1rem)] overflow-hidden rounded-lg border border-white/10 bg-surface-900 shadow-glow sm:h-[min(88%,560px)]"
        >
          <img src={selectedMap.image} alt={selectedMap.name} draggable={false} className="absolute inset-0 h-full w-full select-none object-contain" />
          {selectedMap.zones.map((zone) => (
            <div
              key={zone.id}
              className="absolute z-10 grid -translate-x-1/2 -translate-y-1/2 place-items-center rounded-full border border-emerald-200/45 bg-emerald-300/18 px-2 py-1 text-[10px] font-semibold text-emerald-50 shadow-[0_0_24px_rgba(110,231,183,.35)] animate-pulse sm:px-3 sm:text-xs"
              style={{
                left: `${(zone.x + 0.5) * (100 / MAP_COLUMNS)}%`,
                top: `${(zone.y + 0.5) * (100 / MAP_ROWS)}%`,
              }}
            >
              {zone.label}
            </div>
          ))}
          <Character key={selectedMap.id} position={position} direction={direction} frame={frame} sceneSize={sceneSize} />
        </div>
      </div>

      {isGameReady && <MapSelector maps={MAP_DATA} selectedMapId={selectedMap.id} onSelect={handleMapSelect} />}
      {isGameReady && <MobileJoypad onStart={startMoving} onStop={stopMoving} />}
    </section>
  );
}
