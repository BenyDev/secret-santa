import { GlassControl } from './GlassControl'
import logo from '../secret-santa.svg'

type LandingHeaderProps = {
  isNight: boolean
  onToggleTheme: () => void
}

export function LandingHeader({ isNight, onToggleTheme }: LandingHeaderProps) {
  return (
    <header className="landing-header relative z-10 flex flex-wrap items-center justify-between gap-2">
      <a className="brand flex min-h-12 items-center gap-2 font-semibold" href="/">
        <img className="brand-mark" src={logo} width="32" height="32" alt="" />
        <span>Secret Santa</span>
      </a>

      <div className="flex items-center gap-2 sm:gap-3">
        <GlassControl className="theme-toggle" aria-label="Tryb nocny" aria-pressed={isNight} onClick={onToggleTheme}>
          <span className="theme-option theme-option-day" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round">
              <circle cx="12" cy="12" r="4" />
              <path d="M12 2v2m0 16v2M2 12h2m16 0h2M5 5l1.5 1.5m11 11L19 19m0-14-1.5 1.5m-11 11L5 19" />
            </svg>
          </span>
          <span className="theme-option theme-option-night" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round">
              <path d="M20.8 15.4a8.9 8.9 0 0 1-12.2-12 9.1 9.1 0 1 0 12.2 12Z" />
            </svg>
          </span>
        </GlassControl>
        <GlassControl className="login-control" href="/logowanie">Zaloguj się</GlassControl>
      </div>
    </header>
  )
}
