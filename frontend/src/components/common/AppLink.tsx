import type { AnchorHTMLAttributes, MouseEvent, ReactNode } from "react";
import { Link } from "react-router-dom";

type AppLinkProps = AnchorHTMLAttributes<HTMLAnchorElement> & {
  children: ReactNode;
};

function isInternalPath(href: string) {
  return href.startsWith("/") && !href.startsWith("//");
}

export function AppLink({ children, href = "#", onClick, ...props }: AppLinkProps) {
  const handleClick = (event: MouseEvent<HTMLAnchorElement>) => {
    onClick?.(event);
  };

  if (isInternalPath(href)) {
    return (
      <Link to={href} onClick={handleClick} {...props}>
        {children}
      </Link>
    );
  }

  return (
    <a href={href} onClick={handleClick} {...props}>
      {children}
    </a>
  );
}
