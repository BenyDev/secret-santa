import type { CSSProperties } from 'react'
import treeImage from '../assets/landing/tree/christmas-tree.webp'
import treePoints from '../assets/landing/tree/christmas-tree.points.json'

const { imageSize, anchor, bulbs, star } = treePoints
const treeStyle = {
  '--anchor-x': `${anchor.x / imageSize.width * 100}%`,
  '--anchor-y': `${anchor.y / imageSize.height * 100}%`,
} as CSSProperties

export function ChristmasTree() {
  return (
    <div className="christmas-tree" style={treeStyle} aria-hidden="true">
      <div className="tree-shadow" />
      <div className="tree-ground-light" />
      <img className="tree-image" src={treeImage} width={imageSize.width} height={imageSize.height} alt="" fetchPriority="high" />
      <svg className="tree-lights" viewBox={`0 0 ${imageSize.width} ${imageSize.height}`}>
        <defs>
          <radialGradient id="bulb-glow">
            <stop offset="0" stopColor="#fff9d8" stopOpacity=".95" />
            <stop offset=".26" stopColor="#ffe3a1" stopOpacity=".8" />
            <stop offset=".48" stopColor="#ffc572" stopOpacity=".28" />
            <stop offset=".76" stopColor="#f7b957" stopOpacity=".07" />
            <stop offset="1" stopColor="#f7b957" stopOpacity="0" />
          </radialGradient>
          <radialGradient id="top-star-glow">
            <stop offset="0" stopColor="#fff3bd" stopOpacity=".55" />
            <stop offset=".53" stopColor="#ffd27c" stopOpacity=".18" />
            <stop offset="1" stopColor="#ffd27c" stopOpacity="0" />
          </radialGradient>
        </defs>
        <g className="tree-bulbs">
          {bulbs.map(({ id, x, y }, index) => (
            <circle
              key={id}
              className="tree-bulb"
              cx={x}
              cy={y}
              r="57"
              fill="url(#bulb-glow)"
              style={{
                animationDuration: `${6.5 + index * 0.27}s`,
                animationDelay: `${index * 0.38}s`,
              }}
            />
          ))}
        </g>
        <circle className="tree-top-star" cx={star.x} cy={star.y} r="175" fill="url(#top-star-glow)" />
      </svg>
    </div>
  )
}
