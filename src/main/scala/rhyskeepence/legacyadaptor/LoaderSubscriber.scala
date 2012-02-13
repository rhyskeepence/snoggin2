package rhyskeepence.legacyadaptor

import rhyskeepence.caching.SnogginCache

trait LoaderSubscriber {
  def notifyLoadFinished()
}

class InvalidateCacheAfterLoad(cache: SnogginCache) extends LoaderSubscriber {
  def notifyLoadFinished() {
    cache.invalidate()
  }
}
