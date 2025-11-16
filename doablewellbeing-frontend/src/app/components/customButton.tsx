// app/components/CustomButton.tsx
'use client';
import Link from 'next/link';
import { forwardRef, ButtonHTMLAttributes, AnchorHTMLAttributes } from 'react';
import clsx from 'clsx';

type BaseProps = {
  variant?: 'primary' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
  loading?: boolean;
  className?: string;
  children?: React.ReactNode;
};

type ButtonProps = BaseProps &
  Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'className'> & {
    href?: undefined;
  };

type LinkProps = BaseProps &
  Omit<AnchorHTMLAttributes<HTMLAnchorElement>, 'className'> & {
    href: string;
  };

type Props = ButtonProps | LinkProps;

const styles = {
  base:
    'inline-flex items-center justify-center rounded-full font-semibold transition focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed',
  variant: {
    primary:
      'text-white bg-[#6A5AF9] hover:brightness-95 focus-visible:ring-[#6A5AF9]',
    outline:
      'bg-white text-black border border-black hover:bg-black hover:text-white focus-visible:ring-black',
    ghost:
      'bg-transparent text-black hover:bg-black/5 focus-visible:ring-black',
  },
  size: {
    sm: 'px-4 py-2 text-sm',
    md: 'px-6 py-3 text-base',
    lg: 'px-8 py-4 text-lg',
  },
  full: 'w-full',
};

function getClasses({ variant = 'primary', size = 'md', fullWidth, className }: BaseProps) {
  return clsx(
    styles.base,
    styles.variant[variant],
    styles.size[size],
    fullWidth && styles.full,
    className
  );
}

const CustomButton = forwardRef<HTMLButtonElement & HTMLAnchorElement, Props>(
  ({ variant = 'primary', size = 'md', fullWidth, loading, className, children, ...rest }, ref) => {
    const common = {
      className: getClasses({ variant, size, fullWidth, className }),
      'aria-busy': loading || undefined,
    };

    if ('href' in rest && rest.href) {
      return (
        <Link ref={ref as any} {...rest} {...common}>
          {children}
        </Link>
      );
    }

    return (
      <button ref={ref as any} type="button" {...(rest as ButtonProps)} {...common}>
        {loading ? 'Loading…' : children}
      </button>
    );
  }
);

CustomButton.displayName = 'CustomButton';
export default CustomButton;
