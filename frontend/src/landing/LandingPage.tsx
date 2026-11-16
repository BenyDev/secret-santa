import { motion } from 'motion/react'
import { useState } from 'react'
import { GlassControl } from './GlassControl'
import { LandingActions } from './LandingActions'
import { LandingHeader } from './LandingHeader'
import { WinterScene } from './WinterScene'
import { useSceneLighting } from './useSceneLighting'

const themeKey = 'secret-santa-theme'

const destinationTitles = new Map([
  ['/organizuj', 'Zorganizuj losowanie'],
  ['/dolacz', 'Dołącz do wydarzenia'],
  ['/logowanie', 'Zaloguj się'],
])

function getInitialTheme() {
  try {
    return window.localStorage.getItem(themeKey) === 'night'
  } catch {
    return false
  }
}

export function LandingPage() {
  const [isNight, setIsNight] = useState(getInitialTheme)
  const lighting = useSceneLighting(isNight)
  const destinationTitle = destinationTitles.get(window.location.pathname.replace(/\/$/, ''))

  function toggleTheme() {
    const nextIsNight = !isNight
    setIsNight(nextIsNight)
    try {
      window.localStorage.setItem(themeKey, nextIsNight ? 'night' : 'day')
    } catch {
      // The current theme still works when browser storage is unavailable.
    }
  }

  return (
    <motion.div className="landing relative isolate flex flex-col" data-theme={isNight ? 'night' : 'day'} style={lighting}>
      <LandingHeader isNight={isNight} onToggleTheme={toggleTheme} />
      <main className="landing-main flex flex-1 flex-col">
        <WinterScene />
        {destinationTitle ? (
          <div className="landing-actions relative z-10 grid justify-items-center gap-3 text-center">
            <h1 className="landing-title">{destinationTitle}</h1>
            <p>Ten ekran jest w przygotowaniu.</p>
            <GlassControl href="/">Wróć na stronę główną</GlassControl>
          </div>
        ) : <LandingActions />}
      </main>
    </motion.div>
  )
}
