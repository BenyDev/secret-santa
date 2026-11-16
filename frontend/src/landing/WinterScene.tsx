import type { CSSProperties } from 'react'
import { ChristmasTree } from './ChristmasTree'

const stars = [
  [6, 22], [12, 8], [17, 16], [21, 5], [24, 34], [28, 24], [31, 10],
  [35, 32], [38, 7], [40, 19], [43, 13], [45, 29], [48, 5], [51, 24],
  [54, 11], [57, 34], [59, 22], [62, 7], [65, 17], [68, 28], [71, 12],
  [74, 22], [78, 8], [81, 31], [85, 14], [89, 25], [92, 9], [95, 21],
]

const DESKTOP_FLAKES = 52
const MOBILE_FLAKES = 28

const flakeShapes = [
  'polygon(8% 39%, 22% 27%, 27% 8%, 48% 15%, 64% 5%, 79% 23%, 80% 38%, 96% 53%, 81% 75%, 63% 79%, 48% 96%, 33% 84%, 17% 85%, 8% 64%)',
  'polygon(4% 49%, 19% 25%, 40% 27%, 52% 9%, 68% 19%, 91% 25%, 87% 43%, 99% 58%, 77% 74%, 55% 72%, 40% 90%, 17% 77%, 19% 63%)',
  'polygon(18% 18%, 39% 19%, 51% 3%, 70% 13%, 72% 31%, 89% 47%, 80% 62%, 88% 76%, 68% 88%, 51% 96%, 30% 84%, 31% 65%, 12% 51%)',
  'polygon(7% 32%, 28% 25%, 42% 9%, 59% 23%, 78% 18%, 94% 37%, 83% 56%, 92% 68%, 69% 81%, 50% 76%, 35% 94%, 15% 80%, 21% 61%, 6% 53%)',
  'polygon(12% 42%, 13% 23%, 34% 17%, 43% 6%, 62% 16%, 67% 33%, 85% 38%, 96% 57%, 77% 67%, 63% 88%, 44% 80%, 24% 89%, 20% 69%, 5% 59%)',
  'polygon(4% 51%, 18% 35%, 37% 40%, 50% 16%, 65% 22%, 87% 18%, 96% 41%, 82% 51%, 83% 66%, 62% 73%, 47% 92%, 34% 80%, 15% 76%)',
]

const snowflakes = Array.from({ length: DESKTOP_FLAKES }, (_, index) => {
  const duration = 14 + (index * 7) % 13
  const slot = index * MOBILE_FLAKES / DESKTOP_FLAKES
  const direction = index % 2 === 0 ? 1 : -1

  // Keep the mobile subset spread across the same columns as the desktop layer.
  return {
    wideOnly: Math.floor(slot) === Math.floor((index + 1) * MOBILE_FLAKES / DESKTOP_FLAKES),
    style: {
      left: `${(index + 0.5) * 100 / DESKTOP_FLAKES}%`,
      animationDuration: `${duration}s`,
      animationDelay: `-${duration * ((index * 23) % DESKTOP_FLAKES) / DESKTOP_FLAKES}s`,
      '--flake-size': `${5 + index % 5}px`,
      '--flake-shape': flakeShapes[(index * 5 + Math.floor(index / 7)) % flakeShapes.length],
      '--flake-softness': `${[0.12, 0.23, 0.36, 0.48][(index * 3 + Math.floor(index / 5)) % 4]}px`,
      '--flake-opacity': 0.78 + (index % 4) * 0.07,
      '--flake-drift': `${(5 + (index * 3) % 9) * direction}px`,
      '--flake-sway': `${(4 + (index * 7) % 8) * direction}px`,
      '--flake-tilt': `${(7 + (index * 3) % 13) * direction}deg`,
      '--flake-midpoint': `${48 + (index * 7) % 9}%`,
    } as CSSProperties,
  }
})

export function WinterScene() {
  return (
    <>
      <div className="winter-backdrop pointer-events-none absolute inset-0" aria-hidden="true">
        <div className="landscape-image absolute inset-0" />
        <div className="sky-layer absolute inset-0">
          <div className="sky-stars absolute inset-0">
            {stars.map(([x, y], index) => (
              <i
                className="sky-star"
                key={x}
                style={{ left: `${x}%`, top: `${y}%`, opacity: index % 3 === 0 ? 0.9 : 0.48 }}
              />
            ))}
          </div>
        </div>
        <div className="landscape-shading absolute inset-0" />
      </div>
      <div className="tree-stage pointer-events-none relative grid place-items-center">
        <ChristmasTree />
      </div>
      <div className="scene-snow pointer-events-none absolute inset-0" aria-hidden="true">
        {snowflakes.map(({ style, wideOnly }, index) => (
          <span className="snow-track" key={index} data-wide-only={wideOnly || undefined} style={style}>
            <span className="snow-flake" />
          </span>
        ))}
      </div>
    </>
  )
}
