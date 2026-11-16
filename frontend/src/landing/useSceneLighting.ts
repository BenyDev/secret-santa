import { animate, useMotionValue, useReducedMotion, useTransform } from 'motion/react'
import type { MotionStyle, MotionValue } from 'motion/react'
import { useEffect } from 'react'

// Motion takes durations in seconds.
const SCENE_TRANSITION_SECONDS = 2.4
const REDUCED_TRANSITION_SECONDS = 0.18

type SceneLightingStyle = MotionStyle & {
  '--scene-progress': MotionValue<number>
  '--scene-landscape': MotionValue<number>
  '--scene-stars': MotionValue<number>
  '--scene-bulbs': MotionValue<number>
  '--scene-tree-star': MotionValue<number>
  '--scene-reflection': MotionValue<number>
  '--scene-bulb-pulse': MotionValue<'running' | 'paused'>
  '--scene-snow': MotionValue<number>
}

function smoothstep(progress: number) {
  return progress * progress * (3 - 2 * progress)
}

export function useSceneLighting(isNight: boolean): SceneLightingStyle {
  const target = isNight ? 1 : 0
  const progress = useMotionValue(target)
  const prefersReducedMotion = useReducedMotion()

  useEffect(() => {
    const distance = Math.abs(target - progress.get())
    if (distance === 0) return

    const fullDuration = prefersReducedMotion ? REDUCED_TRANSITION_SECONDS : SCENE_TRANSITION_SECONDS
    const playback = animate(progress, target, {
      ease: 'linear',
      duration: fullDuration * distance,
    })

    return () => playback.stop()
  }, [progress, target, prefersReducedMotion])

  // Each range describes a portion of the same timeline, in either direction.
  const landscape = useTransform(progress, [0.125, 0.625], [0, 1], { ease: smoothstep })
  const stars = useTransform(progress, [0.29, 0.71], [0, 1], { ease: smoothstep })
  const bulbs = useTransform(progress, [1 / 2.4, 2 / 2.4], [0, 1], { ease: smoothstep })
  const treeStar = useTransform(progress, [1.3 / 2.4, 2.2 / 2.4], [0, 1], { ease: smoothstep })
  const reflection = useTransform(progress, [1.15 / 2.4, 2.4 / 2.4], [0, 1], { ease: smoothstep })
  const bulbPulse = useTransform(progress, (value) => value === 1 ? 'running' : 'paused')
  const snow = useTransform(progress, [0, 1], [0.86, 1])

  return {
    '--scene-progress': progress,
    '--scene-landscape': landscape,
    '--scene-stars': stars,
    '--scene-bulbs': bulbs,
    '--scene-tree-star': treeStar,
    '--scene-reflection': reflection,
    '--scene-bulb-pulse': bulbPulse,
    '--scene-snow': snow,
  }
}
