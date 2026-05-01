export const appRouteChangeEvent = "app-route-change";

export function isInternalPath(href: string) {
  return href.startsWith("/") && !href.startsWith("//");
}

export function navigateTo(href: string) {
  if (!isInternalPath(href)) {
    window.location.href = href;
    return;
  }

  if (window.location.pathname !== href) {
    window.history.pushState({}, "", href);
    window.dispatchEvent(new Event(appRouteChangeEvent));
  }

  window.scrollTo({ top: 0, behavior: "smooth" });
}
