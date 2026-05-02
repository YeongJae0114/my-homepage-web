import { navigationItems } from "../../config/navigation";
import { AppLink } from "../common/AppLink";
import { Card } from "../common/Card";

type MenuModalProps = {
  isOpen: boolean;
  onClose: () => void;
};

export function MenuModal({ isOpen, onClose }: MenuModalProps) {
  if (!isOpen) {
    return null;
  }

  const links = navigationItems.filter((item) => item.enabled).sort((a, b) => a.order - b.order);

  return (
    <div className="fixed inset-0 z-[60] grid place-items-center bg-surface-950/82 px-5 backdrop-blur">
      <Card className="w-full max-w-lg p-6">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="font-mono text-xs uppercase text-emerald-200">Navigation</p>
            <h2 className="mt-2 text-2xl font-semibold text-zinc-50">일반 메뉴로 보기</h2>
          </div>
          <button type="button" className="rounded-md border border-white/10 px-3 py-2 text-sm text-zinc-300 hover:bg-white/[0.06]" onClick={onClose}>
            닫기
          </button>
        </div>
        <div className="mt-6 grid gap-2">
          {links.map((link) => (
            <AppLink
              key={link.href}
              href={link.href}
              className="rounded-md border border-white/10 px-4 py-3 text-zinc-200 transition hover:border-emerald-300/30 hover:bg-emerald-300/10"
              onClick={onClose}
            >
              {link.label}
            </AppLink>
          ))}
        </div>
      </Card>
    </div>
  );
}
