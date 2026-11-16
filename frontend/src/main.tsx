import '@fontsource-variable/dm-sans'
import '@fontsource-variable/playfair-display'
import '@fontsource-variable/playfair-display/wght-italic.css'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { LandingPage } from './landing/LandingPage'
import './styles.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <LandingPage />
  </StrictMode>,
)
