package snoggin.caching

import net.sf.ehcache.{Element, CacheManager}

class SnogginCache {
  private lazy val ehCache = CacheManager.create.getEhcache("snoggin")

  def put[T](key: String, value: T) {
    ehCache.put(new Element(key, value))
  }

  def putShortLived[T](key: String, value: T) {
    val element = new Element(key, value)
    element.setTimeToLive(120)
    ehCache.put(element)
  }

  def get[T](key: String): Option[T] = {
    val element = ehCache.get(key)
    if (element != null)
      Some(element.getValue.asInstanceOf[T])
    else
      None
  }

  def invalidate() {
    ehCache.removeAll()
  }

}

