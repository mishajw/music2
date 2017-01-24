package sheetjson

import sheetjson.util.Time.Bars

package object player {
  case class PlayerSpec(
    `type`: Option[String] = None,
    name: Option[String] = None,
    volume: Option[Double] = None,
    lifeTime: Option[Bars] = None,
    speed: Option[Double] = None,
    visible: Boolean = true)
}
