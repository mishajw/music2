package sheetjson.player.activatable

import sheetjson.input.KeyListener.KeyCode
import sheetjson.player.activatable.InteractivePlayer.InteractiveSpec
import sheetjson.player.activatable.SingleKeyInteractivePlayer.SingleKeyInteractiveSpec

trait SingleKeyInteractivePlayer extends InteractivePlayer[SingleKeyInteractiveSpec] {

  protected var _isActive = false

  def isActive = _isActive

  override def keyPressed(kc: KeyCode): Unit = activate()

  override def keyReleased(kc: KeyCode): Unit = deactivate()

  final def activate() = {
    _isActive = true
    _activate()
  }

  final def deactivate() = {
    _isActive = false
    _deactivate()
  }

  protected def _activate()

  protected def _deactivate()
}

object SingleKeyInteractivePlayer {
  case class SingleKeyInteractiveSpec(key: Option[Int])
      extends InteractiveSpec {

    override def keys = key match {
      case Some(k) => Seq(k)
      case None => Seq()
    }
  }
}