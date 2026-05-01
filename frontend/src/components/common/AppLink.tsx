import type { AnchorHTMLAttributes, MouseEvent, ReactNode } from "react";
import { navigateTo, isInternalPath } from "../../utils/router";

type AppLinkProps = AnchorHTMLAttributes<HTMLAnchorElement> & {
  children: ReactNode;
};

export function AppLink({ children, href = "#", onClick, ...props }: AppLinkProps) {
  const handleClick = (event: MouseEvent<HTMLAnchorElement>) => {
    onClick?.(event);

    if (event.defaultPrevented || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
      return;
    }

    if (isInternalPath(href)) {
      event.preventDefault();
      navigateTo(href);
    }
  };

  return (
    <a href={href} onClick={handleClick} {...props}>
      {children}
    </a>
  );
}
