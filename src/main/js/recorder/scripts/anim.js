// Util
const setRandom = () => {
  let ranX = Math.random() * window.innerWidth,
    ranY = Math.random() * window.innerHeight

  while (isInBounds(ranX, ranY)) {
    ranX = Math.random() * window.innerWidth
    ranY = Math.random() * window.innerHeight
  }

  target.style.left = ranX + 'px'
  target.style.top = ranY + 'px'
}

const isInBounds = (x, y) => {
  return x >= bounds.x && x <= bounds.x + bounds.width && y >= bounds.y && y <= bounds.y + bounds.height
}

// Loop
const animate = () => {
  // Fade In/Out Red Circle
  if (blinker && frame++ >= 59) {
    frame = 0

    if (blinkCounter++ >= 2) {
      blinker.style.opacity = blinker.style.opacity == 0.4 ? 0.8 : 0.4
      blinkCounter = 0
    }
  }

  // Loop
  window.requestAnimationFrame(animate)
}

window.onload = () => {
  // Cache DOM elements

  blinker = document.getElementById('blinker')
  target = document.getElementById('target')

  pathCount = document.getElementById('paths')
  saveLink = document.getElementById('save')

  // Calculate bounds to restrict target
  let flairBounds = document.getElementById('flair').getBoundingClientRect(),
    infoBounds = document.getElementById('info').getBoundingClientRect()

  bounds = {
    x: flairBounds.x,
    y: flairBounds.y,
    width: flairBounds.width,
    height: flairBounds.height + infoBounds.height,
  }

  console.log(bounds)

  // Init loop
  window.requestAnimationFrame(animate)
}
