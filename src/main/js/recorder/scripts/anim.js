const animate = () => {
  if (!startPos) {
    startPos = document.getElementById('start')
    blinker = document.getElementById('blinker')
    saveLink = document.getElementById('save')
    pathCount = document.getElementById('paths')
  } else {
    // Current start point of path builder
    let pos = builder ? (builder.points.length < 1 ? undefined : builder.points[0]) : undefined

    if (pos) {
      // Ignore if mouse is over info
      let bounds = saveLink.getBoundingClientRect()

      let xInBounds = pos.x >= bounds.x && pos.x <= bounds.x + bounds.width,
        yInBounds = pos.y >= bounds.y && pos.y <= bounds.y + bounds.height

      // Prevent moving on top of save link
      if (!(xInBounds && yInBounds)) {
        // Set start div to current mouse position
        startPos.style.left = pos.ox - 5 + 'px'
        startPos.style.top = pos.oy - 5 + 'px'
      }
    }

    // Fade In/Out Red Circle
    if (frame++ >= 59) {
      frame = 0

      if (blinkCounter++ >= 2) {
        blinker.style.opacity = blinker.style.opacity == 0.4 ? 0.8 : 0.4
        blinkCounter = 0
      }
    }
  }

  // Loop
  window.requestAnimationFrame(animate)
}

// Init loop
window.requestAnimationFrame(animate)
