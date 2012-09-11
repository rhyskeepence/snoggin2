package snoggin.storage

import net.liftweb.util.Props
import com.mongodb.casbah.{MongoCollection, MongoConnection}

class MongoStorage(host: String, port: Int) {

  def this() = this(Props.get("mongo.host", "localhost"), Props.getInt("mongo.port", 27017))

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