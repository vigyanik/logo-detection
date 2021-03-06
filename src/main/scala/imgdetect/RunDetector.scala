package imgdetect

import imgretrieve.DiskImageKeeper
import messages.{ DetectMessage, KafkaClient, StoreDetectedMessage }
import messages.serializers.MessageSerializers

import org.opencv.core.Core

object RunDetector {

  val INTOPIC = "toDetect"
  val OUTTOPIC = "toStoreDetected"
  val trainLogos = Array("google", "facebook", "hootsuite")

  def main(args: Array[String]) {
    // load native OpenCV library
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    import MessageSerializers._

    val keeper = new DiskImageKeeper
    val detector = new LogoDetector(trainLogos)
    val kafkaConsumer = new KafkaClient[DetectMessage](INTOPIC)
    val kafkaProducer = new KafkaClient[StoreDetectedMessage](OUTTOPIC)

    //kafkaConsumer.consumerStart()

    while (true) {
      val msg = kafkaConsumer.receive()
      val matchedLogos = detector.detect(msg.imageLink)

      if (!matchedLogos.isEmpty) {
        val storeDetectedMsg = new StoreDetectedMessage(msg.imageLink, matchedLogos)
        kafkaProducer.send(storeDetectedMsg)
      }
    }
  }
}