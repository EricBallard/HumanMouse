// Data Structures
function MousePos(x, y, delay) {
    this.x = x;
    this.y = y;
    this.delay = delay
}

function MousePath() {
    this.xSpan = 0
    this.ySpan = 0
    this.totalTime = 0
    this.totalPoints = 0
    this.points = []

    const add = (pos) => {
        this.points.push(pos)

        this.totalPoints++
        this.totalTime += pos.delay
    }
}
