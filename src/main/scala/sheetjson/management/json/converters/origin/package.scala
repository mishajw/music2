package sheetjson.management.json.converters

import org.json4s.JObject
import org.json4s.JsonAST.{JDouble, JString}
import sheetjson.player.WaveFunction
import sheetjson.player.composite.KeyboardScale
import sheetjson.player.origin.{FadingNoise, RawFile, Tone}
import sheetjson.util.Frequencies.FrequencyOf
import sheetjson.util.Time.Bars
import sheetjson.util.{Notes, Scales}

package object origin {

  /**
    * Convert to `Tone`
    */
  implicit object ToneConverter extends JsonConverter[Tone] {

    case class JTone(note: Double, waveFunction: String = "sine")

    override def applyOpt(json: JObject): Option[Tone] = {
      // Transform note strings into frequencies
      val transformed = json transformField {
        case ("note", JString(n)) =>
          Notes noteFor n match {
            case Some(note) => ("note", JDouble(note frequency))
            case None => "note" -> JString(n)
          }
      }

      for {
        note <- (transformed \ "note").extractOpt[Double]
        waveFunction = (transformed \ "waveFunction").extractOrElse("sine": String)
        wave <- WaveFunction getOpt waveFunction
      } yield new Tone(note, wave, getSpec(json))
    }
  }

  /**
    * Convert to `FadingNoise`
    */
  implicit object FadingNoiseConverter extends JsonConverter[FadingNoise] {
    override def applyOpt(json: JObject): Option[FadingNoise] = json \ "length" match {
      case JDouble(length) => Some(new FadingNoise(Bars(length), getSpec(json)))
      case _ => None
    }
  }

  /**
    * Convert to `Keyboard`
    */
  implicit object KeyboardScaleConverter extends JsonConverter[KeyboardScale] {
    /**
      * @param json the JSON object to convert
      * @return the converted `Player` object
      */
    override def applyOpt(json: JObject): Option[KeyboardScale] = {
      for {
        JString(scale) <- Option(json \ "scale")
        JString(key) <- Option(json \ "key")
        note <- Notes relativeNoteFor key
        scale <- Scales get(note, scale)
      } yield new KeyboardScale(scale, getSpec(json))
    }
  }

  /**
    * Convert to `RawFile`
    */
  implicit object RawFileConverter extends JsonConverter[RawFile] {
    override def applyOpt(json: JObject): Option[RawFile] = {
      for {
        path <- (json \ "path").extractOpt[String]
      } yield new RawFile(path, getSpec(json))
    }
  }
}
