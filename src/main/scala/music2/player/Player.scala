package music2.player

/**
  * Represents something that "plays" music
  */
abstract class Player(val playerSpec: PlayerSpec) {

  /**
    * How many times play has been called on this `Player`
    */
  private var absoluteStep: Double = 0

  /**
    * The speed of the `Player`
    */
  var speed: Double = playerSpec.speed getOrElse 1

  /**
    * How long a `Player` plays for
    */
  val lifeTime: Option[Double] = playerSpec.lifeTime

  /**
    * How loud a `Player` is
    */
  val volume: Double = playerSpec.volume getOrElse 1

  /**
    * @return the next value played, and handle other transformations
    */
  def play: Playable = {
    if (!alive) return Playable.default

    val played = _play
    absoluteStep += 1
    played
  }

  /**
    * @return the next value played
    */
  protected def _play: Playable

  /**
    * @return the relative step, scaled by speed and sample rate
    */
  def step = (absoluteStep * speed) / music2.sampleRate

  /**
    * @return whether the `Player` is alive
    */
  def alive: Boolean = lifeTime match {
    case Some(lt) => step <= lt
    case None => true
  }
}
