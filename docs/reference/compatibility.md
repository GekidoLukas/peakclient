# Compatibility

## Versions

| | |
| --- | --- |
| Minecraft | 1.21.1 |
| Loaders | Fabric and NeoForge |
| Java | 21 |
| Fabric API | required on Fabric |

## Where the mod is needed

PeakClient has two sides in one mod. Install it on the server and on every client that should see the interfaces.

- The **server** needs it so that your server mod can use the API.
- A **client** needs it to draw what the server sends.

Players without the mod are skipped silently, they just see nothing. See
[Threads and Timing](/concepts/threads-and-timing).

## Joining a server

| Server loader | Player without the mod |
| --- | --- |
| Fabric | can join and plays without the interfaces |
| NeoForge | cannot join, because the mod's network channels are required |

If you want players without the mod on a NeoForge server, tell them to install it, or use a Fabric server.

## Matching versions

Server and client must use the same PeakClient protocol version. A client with a different version still connects, but the
server logs a warning, and elements or fields may be missing or look different. Keep both on the same release of the mod.

## License

PeakClient is licensed under the GNU General Public License v3.0. The source is at
[github.com/GekidoLukas/peakclient](https://github.com/GekidoLukas/peakclient).
