package rhyskeepence.storage

import net.liftweb.util.Props
import com.mongodb.casbah.{MongoCollection, MongoConnection}

class MongoStorage {
  private lazy val host = Props.get("mongo.host", "localhost")
  private lazy val port = Props.getInt("mongo.port", 27017)

  def withCollection[T](collectionName: String)(actionOnCollection: MongoCollection => T) = {
    val mongo = MongoConnection(host, port)
    val snoggin = mongo("snoggin")

    try {
      val collection = snoggin(collectionName)
      actionOnCollection(collection)

    } finally {
      mongo.close()
    }
  }

  def collectionNames = {
    val mongo = MongoConnection(host, port)

    try mongo("snoggin").getCollectionNames()
    finally mongo.close()
  }
}