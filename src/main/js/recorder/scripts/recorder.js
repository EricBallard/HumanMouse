// Track and cache efficient mouse paths

/*
 For the purpose of this demo, a valid MousePath is as follows;

    A valid path will be discarded if it contains less than 10 points
    or should the entirety of the path exceed 1.5s

    A potential path will be reset if the cursor is idle for 256-384ms

*/

// Potential path
let builder = null

// Time of last move
let lastMove = 0

const reset = () => {
  builder = null
  lastMove = 0
}

const onMouseMove = e => {
  let now = Date.now(),
    delay = lastMove == 0 ? 0 : now - lastMove

  // Restart path if cursor was not moved for 256-384ms
  if (lastMove != 0 && delay > Math.random() * 128 + 256) {
    //log.debug("@Recorder ~ Resetting Path, delay: " + delay + ", points: " + path.points.size());
    delay = 0
    reset()
  }

  // Start new path if needed
  if (builder == null) builder = new MousePath()

  // Add point to builder
  builder.add(new MousePoint(e.pageX, e.pageY, delay))
  lastMove = now
}

const onMouseClick = e => {
  if (builder == null || builder.points.length == 0) {
    console.warn('@Recorder~ Builder does not have a path or it is empty.')
    reset()
    return
  }

  let delay = Date.now() - lastMove,
    totalTime = builder.getTotalTime()

  if (builder.totalPoints < 10 || delay >= 512 || totalTime > 1536) {
    console.debug('@Recorder~ Ignoring path, click was delayed or total time is too long.')
    reset()
    return
  }

  builder.calculate()
  paths.add(builder)

  console.info('@Recorder~ Registered path of ' + totalTime + 'ms (' + builder.totalPoints + ')')
  pathCount.innerText = paths.totalPaths
  reset()
}

// Register events
document.addEventListener('mousemove', e => onMouseMove(e))
document.addEventListener('click', e => onMouseClick(e))

document.addEventListener('mouseleave', reset())
