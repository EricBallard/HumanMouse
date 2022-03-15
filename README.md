# HumanMouse
_Tool suite designed to manage mouse movement, record and replay human paths within desired bounds._

## Features
- **Cross-platform**, fully supports; Windows, Linux, & MacOS
<br></br>
- Record user mouse movement and replay paths 
- Translate paths from reference to local coordinates
- Transform paths to touch a specific screen coordinate
<br></br>
- Load and manage collections of paths in JSON format
- Select and combine multiple JSON collections into one
- Manually or Automatically test a collection (configurable)
- Pack JSON paths into a SQLite3 database for use in production

## Details
HumanMouse intends to serve as an optional replacement for the variety of existing artifical human mouse-path-generators.  
An overwhelming majority of these libaries use an approach known as [WindMouse](https://ben.land/post/2021/04/25/windmouse-human-mouse-movement/).
This algorthim is over a decade old and widely used.  Henceforth the creation of HumanMouse. Depending on your project scope and the importance of obscurity, this may or may not be for you.

### WindMouse
| <img width="100px"/>PROS<img width="100px"/> | <img width="100px"/>CONS<img width="100px"/> |
| :----: | :-----------: |
| No storage  | Old algorithm |
| Quick generation | Potentially detectable |
| &nbsp; | Requires configuration |

### HumanMouse
| <img width="100px"/>PROS<img width="100px"/> | <img width="100px"/>CONS<img width="100px"/> |
| :----: | :-----------: |
| Scalable  | Uses storage |
| Human data | Slower than generation |
| No shared footprints  | Requires collected database |

### Recording

### Managing

### Production
