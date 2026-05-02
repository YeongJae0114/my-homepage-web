import type { MapDefinition } from "./mapData";

type MapSelectorProps = {
  maps: MapDefinition[];
  selectedMapId: string;
  onSelect: (mapId: string) => void;
};

export function MapSelector({ maps, selectedMapId, onSelect }: MapSelectorProps) {
  return (
    <label
      className="absolute left-4 top-4 z-30 max-w-[calc(100%-2rem)] rounded-lg border border-white/10 bg-surface-950/75 px-3 py-2 text-sm text-zinc-200 shadow-glow backdrop-blur"
      onClick={(event) => event.stopPropagation()}
      onMouseDown={(event) => event.stopPropagation()}
      onTouchStart={(event) => event.stopPropagation()}
    >
      <span className="mr-2 text-xs font-semibold uppercase text-zinc-500">Map</span>
      <select
        value={selectedMapId}
        onChange={(event) => onSelect(event.target.value)}
        className="touch-auto rounded-md border border-white/10 bg-surface-900 px-2 py-1 text-sm text-zinc-100"
      >
        {maps.map((map) => (
          <option key={map.id} value={map.id}>
            {map.name}
          </option>
        ))}
      </select>
    </label>
  );
}
