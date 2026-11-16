import type { ComponentPropsWithoutRef } from 'react'

type GlassStyle = {
  variant?: 'neutral' | 'primary'
}

type GlassControlProps = GlassStyle & (
  | (ComponentPropsWithoutRef<'a'> & { href: string })
  | (ComponentPropsWithoutRef<'button'> & { href?: never })
)

export function GlassControl(props: GlassControlProps) {
  const className = ['glass-control', props.className].filter(Boolean).join(' ')

  if (props.href !== undefined) {
    const { variant = 'neutral', ...linkProps } = props
    return <a {...linkProps} className={className} data-variant={variant} />
  }

  const { variant = 'neutral', href: _href, ...buttonProps } = props
  return <button type="button" {...buttonProps} className={className} data-variant={variant} />
}
