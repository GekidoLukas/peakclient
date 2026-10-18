# Troubleshooting

## The player sees nothing

Go through this list from top to bottom.

1. **Does the player have the mod?** `PeakClient.hasClient(uuid)` is true only after the client said hello. Right after
   joining it is still false. Send your interface from `PeakClient.onClientReady(...)` or use an audience, which waits for
   the player.
2. **Is the API ready?** Use it after the server has started. `PeakClient.isAvailable()` tells you.
3. **Does the id make sense?** Ids use one `:` and `/` after it (`mygame:hud/title`). An update for an unknown id is ignored
   and logged.
4. **Is the element visible?** Check `hidden`, `opacity`, a size of zero, a position outside the screen, or a parent with
   `overflow` that clips it.
5. **Is the document too large?** Above 1 MiB (compressed) it is not sent. A warning appears at 80 percent. Use views,
   patches and values.
6. **Was it replaced?** Sending a document with an id that is already shown replaces it. Two parts of your code using the
   same id overwrite each other.
7. **Did the player reconnect?** Documents sent with `PeakClient.to(...)` are not sent again. Use an audience with
   `persistent(true)`.

## Log messages

Messages are written to the server log. Validation warnings do not stop a document from being sent. Only a document that is
too large is not sent.

| Message | Meaning | What to do |
| --- | --- | --- |
| `Document validation: Duplicate element id '...' found in document.` | two elements share an id | give every element its own id |
| `Document validation: ... is very large (... bytes compressed ...)` | the document is above 80 percent of the 1 MiB limit | split it, or use patches and values |
| `Document validation: ...` (other texts) | the document has a problem, such as an action or link that points to a missing element, or a binding without a placeholder | read the rest of the text and fix the element it names |
| `Operation validation: Update operation targets element '...'` | an update refers to an element that is not in the document | check the id, and that the element was sent before |
| `Operation validation: Update for element '...' uses key '...'` | the patch contains a field the element does not have | check the field name and the patch type |
| `Operation validation: Add operation references unknown parent '...'` | you add an element to a parent that does not exist | check the parent id |
| `Document '...' was not sent: ... bytes compressed exceed the payload limit` | the document or update is above the limit and was dropped (logged as an error) | send less, or use patches and values |
| `Player ... has no PeakClient channel (vanilla client or not connected yet), packets are skipped` | the player has no mod, or has not finished joining (once per player) | wait for `onClientReady`, or ignore |
| `Player ... uses PeakClient protocol ... but the server expects ...` | server and client mod versions differ | install the same version on both |
| `Menu '...' was not opened for ... player(s) because it was never sent to them` | you opened a menu that was not sent first | send the menu before you open it |

## Something looks wrong

- Press F3 and 0 together to see outlines and details of each element. See [Elements Overview](/elements/overview).
- A bound field that you patch is overwritten by its binding. Call `unbind` first.
- A border takes no space. Add padding. See [Pitfalls](/reference/pitfalls).

If you found a bug, report it at
[github.com/GekidoLukas/peakclient/issues](https://github.com/GekidoLukas/peakclient/issues).
