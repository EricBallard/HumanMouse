// Data Structures
function Paths() {
  this.totalPaths = 0
  this.list = []

  this.add = function (path) {
    this.list.push(path)
    this.totalPaths++
  }
}

function MousePoint(x, y, delay) {
  // OX = Original X, X = difference between this X
  // and parent point's X (calculated on export)
  this.ox = x
  this.x = 0
  this.oy = y
  this.y = 0
  this.delay = delay
}

function MousePath() {
  this.xSpan = 0
  this.ySpan = 0
  this.totalTime = 0
  this.totalPoints = 0
  this.points = []

  this.add = function add(pos) {
    this.points.push(pos)

    this.totalPoints++
    this.totalTime += pos.delay
  }

  this.getTotalTime = function () {
    // Register total time of path
    let totalTime = 0
    this.points.forEach(p => (totalTime += p.delay))

    return totalTime
  }

  this.getSpan = function () {
    // Register x and y span of path
    let start = this.points[0],
      end = this.points[this.points.length - 1]

    this.xSpan = end.ox - start.ox
    this.ySpan = end.oy - start.oy
  }

  this.calculate = function () {
    console.log('calculating path')

    // Register difference between between points
    // (Useful for quick translation)

    // Global
    this.getSpan()

    // Local
    const size = this.totalPoints

    for (let i = 1; i < size; i++) {
      let point = this.points[i],
        prior = this.points[i - 1]

      point.x = point.ox - prior.ox
      point.y = point.oy - prior.oy
    }
  }
}

// Cached Data
const paths = new Paths()

let frame = 0,
  blinkCounter = 0

// DOM elements
let target, blinker, bounds, saveLink, pathCount

// Util
function save() {
  let data = JSON.stringify(paths, null, 2)
  let file = new Blob([data], { type: 'application/json' })

  saveLink.href = URL.createObjectURL(file)
  saveLink.download = 'mouse_paths'
}