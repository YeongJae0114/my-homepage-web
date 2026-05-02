export type Direction = "down" | "up" | "left" | "right";

export type Position = {
  x: number;
  y: number;
};

export type Zone = Position & {
  id: string;
  label: string;
  path: string;
};

export type MapDefinition = {
  id: string;
  name: string;
  image: string;
  startX: number;
  startY: number;
  grid: number[][];
  zones: Zone[];
};

const baseGrid = [
  [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
  [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
  [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
  [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
  [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
  [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
  [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
  [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
  [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
];

export const MAP_COLUMNS = 16;
export const MAP_ROWS = 9;

export const MAP_DATA: MapDefinition[] = [
  {
    id: "studio-apartment",
    name: "Studio Apartment",
    image: "/images/map/studio-apartment.png",
    startX: 7.5,
    startY: 4,
    grid: baseGrid,
    zones: [
      { id: "about", label: "About", path: "/about", x: 9.3, y: 1.5 },
      { id: "service", label: "Service", path: "/service", x: 12, y: 6 },
      { id: "blog", label: "Blog", path: "/blog", x: 4, y: 1.5 },
      { id: "project", label: "Project", path: "/project", x: 3, y: 3.5 },
      { id: "monitoring", label: "Monitoring", path: "/monitoring", x: 12, y: 3.5},
    ],
  },
  {
    id: "tech-garden",
    name: "Tech Garden",
    image: "/images/map/tech-garden.png",
    startX: 7.5,
    startY: 4,
    grid: baseGrid,
    zones: [
      { id: "about", label: "About", path: "/about", x: 7.5, y: 2.1 },
      { id: "service", label: "Service", path: "/service", x: 4, y: 6.7 },
      { id: "blog", label: "Blog", path: "/blog", x: 3, y: 3.5 },
      { id: "project", label: "Project", path: "/project", x: 10.5, y: 3.3 },
      { id: "monitoring", label: "Monitoring", path: "/monitoring", x: 10.5, y: 6.5 },
    ],
  },
  {
    id: "technological-hub",
    name: "Technological Hub",
    image: "/images/map/technological-hub.png",
    startX: 7.5,
    startY: 4,
    grid: baseGrid,
    zones: [
      { id: "about", label: "About", path: "/about", x: 1.9, y: 1.6 },
      { id: "service", label: "Service", path: "/service", x: 2.2, y: 4.6 },
      { id: "blog", label: "Blog", path: "/blog", x: 12.5, y: 4.4 },
      { id: "project", label: "Project", path: "/project", x: 7.6, y: 6.5 },
      { id: "monitoring", label: "Monitoring", path: "/monitoring", x: 7.6, y: 2 },
    ],
  },
];

export const CHARACTER_SPRITE = {
  webp: "/images/character-sprite/character-sprite.webp",
  png: "/images/character-sprite/character-sprite.png",
  columns: 3,
  rows: 4,
  directionRows: {
    down: 0,
    up: 1,
    right: 2,
    left: 3,
  } satisfies Record<Direction, number>,
};
