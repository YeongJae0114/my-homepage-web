import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useOwner } from "./useOwner";

/** True while the user is typing, so the shortcut never steals a keystroke from a form. */
function isTyping(target: EventTarget | null): boolean {
  return target instanceof HTMLElement && (target.isContentEditable || ["INPUT", "TEXTAREA", "SELECT"].includes(target.tagName));
}

/**
 * Ctrl+Alt+O (Mac: Control+Option+O) on any page: owner login, or straight to the portfolio when signed in.
 * Matches the physical key (event.code), so it works with the Korean IME on and across keyboard layouts.
 * Convenience only: the route is not secret, the backend enforces access.
 */
export function useOwnerShortcut() {
  const { owner } = useOwner();
  const navigate = useNavigate();

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.code !== "KeyO" || !event.ctrlKey || !event.altKey || event.shiftKey || event.metaKey) return;
      if (event.repeat || isTyping(event.target)) return;
      event.preventDefault();
      navigate(owner ? "/portfolio" : "/owner");
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [owner, navigate]);
}
