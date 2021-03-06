package sheetjson.output

import java.util.concurrent.LinkedBlockingQueue
import javax.sound.sampled.{AudioSystem, DataLine, SourceDataLine}

import com.typesafe.scalalogging.Logger
import sheetjson.player.{EndPlayable, Playable}
import sheetjson.util.Config

import scala.collection.mutable.ArrayBuffer

/**
  * Takes sound data and plays them through an audio device
  */
class SoundOut extends Out {

  private val log = Logger(getClass)

  /**
    * Input audio is read in these size chunks
    */
  private val bufferAmount = 8

  /**
    * Bytes are read from this queue
    */
  private val byteQueue =
    new LinkedBlockingQueue[Playable]()

  /**
    * How long to wait before playing
    * (in milliseconds)
    */
  private val waitBuffer: Double = 1

  /**
    * The thread that plays bytes
    */
  private val thread = new Thread(new Runnable {
    def run() = playFromQueue()
  })

  /**
    * @return the default audio line
    */
  private def defaultLine = {

    val info =
      new DataLine.Info(classOf[SourceDataLine], Config.format)

    val line =
      AudioSystem.getLine(info).asInstanceOf[SourceDataLine]

    line.open(Config.format, 4096)
    line.start()

    line
  }

  /**
    * Destroy a line
    */
  private def destroy(line: SourceDataLine) = {
    line.drain()
    line.stop()
    line.close()
  }

  /**
    * Read from the byte queue, and play the results
    */
  private def playFromQueue() = {
    implicit val line = defaultLine

    log.debug("Start playing from queue")

    val bytes: ArrayBuffer[Byte] = ArrayBuffer()
    while (playing) {
      val playable = byteQueue.take()

      if (playable.isInstanceOf[EndPlayable]) {
        reachedEnd()
      }

      bytes ++= playable.toBytes

      if (bytes.size > bufferAmount) {
        playBytes(bytes)
        bytes.clear()
      }
    }

    log.debug("Stop playing from queue")

    destroy(line)
  }

  /**
    * Play bytes to audio
    *
    * @param bytes the bytes to play
    * @param line the line to play them from
    */
  private def playBytes(bytes: Traversable[Byte])(implicit line: SourceDataLine) = {
    line.write(bytes.toArray, 0, bytes.size)
  }

  /**
    * Start the playing
    */
  def start(): Unit = thread.start()

  /**
    * Play a playable value
    */
  def play(p: Playable) = {
    val inQueue = byteQueue.size()
    val timeToConsume = (inQueue.toDouble / Config.sampleRate.toDouble) * 1000

    if (timeToConsume > waitBuffer)
      Thread.sleep((timeToConsume - waitBuffer).toInt)

    byteQueue add p
  }

  /**
    * Stop the playing
    */
  def stop() = /*thread.synchronized */{
    thread.join()
  }
}

object SoundOut {
}
