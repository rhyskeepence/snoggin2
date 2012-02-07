package rhyskeepence.caching

import net.sf.ehcache.{Ehcache, Element, CacheManager}

trait Cacheable {

  val cache = new SnogginCache

  def getCachedOrGenerate[T](key: String)(generator: => T): T = {
    val fromCache = cache.get(key)
    fromCache.getOrElse {
      val newValue = generator
      cache.put(key, newValue)
      newValue  
    }
  }

  def invalidateCache() {
    cache.invalidate()
  }
}

class SnogginCache {

  def put[T](key: String, value: T) {
    getSnogginCache.put(new Element(key, value))
  }

  def get[T](key: String): Option[T] = {
    val element = getSnogginCache.get(key)
    if (element != null)
      Some(element.getValue.asInstanceOf[T])
    else
      None
  }
  
  def invalidate() {
    getSnogginCache.removeAll()
  }

  private def getSnogginCache[T]: Ehcache = {
    CacheManager.create.getEhcache("snoggin")
  }
}