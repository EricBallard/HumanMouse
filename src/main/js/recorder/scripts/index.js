// Cached Data
let paths = []
let pos = new MousePos(0, 0)

// DOM elements
let blinker,
  saveLink,
  startPos = undefined

// Track mouse
document.addEventListener('mousemove', e => {
  pos.x = e.pageX
  pos.y = e.pageY
})

// Util
function save() {
  paths.push(new MousePath())

  let data = JSON.stringify(paths, null, 2)
  let file = new Blob([data], {type: 'application/json'})

  saveLink.href = URL.createObjectURL(file)
  saveLink.download = 'mouse_paths'
  saveLink.click()
}

// Loop
let frame = 0,
  blinkCounter = 0

const animate = () => {
  if (!startPos) {
    startPos = document.getElementById('start')
    blinker = document.getElementById('blinker')
    saveLink = document.getElementById('save')
  } else {
    if (pos) {
      // Ignore if mouse is over info
      let bounds = saveLink.getBoundingClientRect()

      let xInBounds = pos.x >= bounds.x && pos.x <= bounds.x + bounds.width,
        yInBounds = pos.y >= bounds.y && pos.y <= bounds.y + bounds.height

        // Prevent moving on top of save link
      if (!(xInBounds && yInBounds)) {
        
          // Set start div to current mouse position
          startPos.style.left = pos.x - 5 + 'px'
          startPos.style.top = pos.y - 5 + 'px'
      }
    }

    if (frame++ >= 59) {
      frame = 0

      if (blinkCounter++ >= 2) {
        blinker.style.opacity = blinker.style.opacity == 0.4 ? 0.8 : 0.4
        blinkCounter = 0
      }
    }
  }

  window.requestAnimationFrame(animate)
}

// Init loop
window.requestAnimationFrame(animate)
