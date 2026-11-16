import { GlassControl } from './GlassControl'

export function LandingActions() {
  return (
    <div className="landing-actions relative z-10 grid justify-items-center text-center">
      <h1 className="landing-title">
        <span>Jedna osoba. Jeden prezent.</span>
        <em>Więcej magii.</em>
      </h1>
      <div className="grid w-full max-w-64 gap-2.5 sm:flex sm:w-auto sm:max-w-none sm:gap-3">
        <GlassControl href="/organizuj" variant="primary">Zorganizuj losowanie</GlassControl>
        <GlassControl href="/dolacz">Dołącz do wydarzenia</GlassControl>
      </div>
    </div>
  )
}
