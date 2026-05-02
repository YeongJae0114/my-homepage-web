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
    startX: 7,
    startY: 5,
    grid: baseGrid,
    zones: [
      { id: "about", label: "About", path: "/about", x: 3, y: 2 },
      { id: "service", label: "Service", path: "/service", x: 12, y: 2 },
      { id: "blog", label: "Blog", path: "/blog", x: 12, y: 6 },
      { id: "project", label: "Project", path: "/project", x: 3, y: 6 },
      { id: "monitoring", label: "Monitoring", path: "/monitoring", x: 8, y: 1 },
    ],
  },
  {
    id: "tech-garden",
    name: "Tech Garden",
    image: "/images/map/tech-garden.png",
    startX: 8,
    startY: 6,
    grid: baseGrid,
    zones: [
      { id: "about", label: "About", path: "/about", x: 2, y: 4 },
      { id: "service", label: "Service", path: "/service", x: 13, y: 4 },
      { id: "blog", label: "Blog", path: "/blog", x: 11, y: 2 },
      { id: "project", label: "Project", path: "/project", x: 5, y: 2 },
      { id: "monitoring", label: "Monitoring", path: "/monitoring", x: 8, y: 7 },
    ],
  },
  {
    id: "technological-hub",
    name: "Technological Hub",
    image: "/images/map/technological-hub.png",
    startX: 7,
    startY: 4,
    grid: baseGrid,
    zones: [
      { id: "about", label: "About", path: "/about", x: 4, y: 1 },
      { id: "service", label: "Service", path: "/service", x: 11, y: 1 },
      { id: "blog", label: "Blog", path: "/blog", x: 13, y: 6 },
      { id: "project", label: "Project", path: "/project", x: 2, y: 6 },
      { id: "monitoring", label: "Monitoring", path: "/monitoring", x: 8, y: 7 },
    ],
  },
];

export const characterFrames: Record<Direction, string[]> = {
  down: [
    "/images/character/01_front_idle.svg",
    "/images/character/02_front_walk_a.svg",
    "/images/character/03_front_walk_b.svg",
  ],
  up: [
    "/images/character/04_back_idle.svg",
    "/images/character/05_back_walk_a.svg",
    "/images/character/06_back_walk_b.svg",
  ],
  right: [
    "/images/character/07_side_right_idle.svg",
    "/images/character/08_side_right_walk_a.svg",
    "/images/character/09_side_right_walk_b.svg",
  ],
  left: [
    "/images/character/10_side_left_idle.svg",
    "/images/character/11_side_left_walk_a.svg",
    "/images/character/12_side_left_walk_b.svg",
  ],
};
