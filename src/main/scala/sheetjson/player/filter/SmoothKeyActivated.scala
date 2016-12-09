package sheetjson.player.filter

import sheetjson.management.KeyListener.KeyCode
import sheetjson.player._
import sheetjson.util.Time.{Absolute, Bars}

import scala.collection.mutable.ArrayBuffer

class SmoothKeyActivated(keyCode: Int,
                         val inFunction: WaveFunction,
                         val outFunction: WaveFunction,
                         val fadeInTime: Bars,
                         val fadeOutTime: Bars,
                         _child: Player,
                         _spec: PlayerSpec) extends FilterPlayer(_child, _spec) with ListenerPlayer {

  override val keys: Seq[KeyCode] = Seq(keyCode)

  val childPlays: ArrayBuffer[Playable] = ArrayBuffer()

  var lastPressed: Option[Absolute] = None
  var lastReleased: Option[Absolute] = None

  override protected def _play: Playable = lastPressed match {
    case Some(absolute) => getChildPlayed(absolute)
    case None => Playable.default
  }

  def getChildPlayed(pressedAbsolute: Absolute): Playable = {
    val sincePressed = Bars(absoluteStep - pressedAbsolute)
    val sincePressedAbsolute = Absolute(sincePressed)

    while (sincePressedAbsolute.toInt >= childPlays.length)
      childPlays += child.play

    val played = childPlays(sincePressedAbsolute.toInt)

    lastReleased match {
      case None =>
        if (sincePressed < fadeInTime) {
          played * inFunction((sincePressed / fadeInTime).toDouble)
        } else {
          played
        }
      case Some(releasedAbsolute) =>
        val sinceRelease = Bars(absoluteStep - releasedAbsolute)
        if (sinceRelease < fadeOutTime) {
          played * outFunction((sinceRelease / fadeOutTime).toDouble)
        } else {
          lastPressed = None
          lastReleased = None
          Playable.default
        }
    }

  }

  override def keyPressed(kc: KeyCode): Unit = {
    if (lastPressed.isEmpty)
      lastPressed = Some(absoluteStep)
  }

  override def keyReleased(kc: KeyCode): Unit = {
    lastReleased = Some(absoluteStep)
  }
}
