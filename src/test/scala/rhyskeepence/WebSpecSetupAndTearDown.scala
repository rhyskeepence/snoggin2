package rhyskeepence

import net.liftweb.mockweb.WebSpec
import bootstrap.liftweb.Boot
import net.liftweb.http.LiftRules

trait WebSpecSetupAndTearDown { _: WebSpec =>
  setup().beforeSpec

  def setup() = new Boot().boot
  def destroy() = LiftRules.unloadHooks.toList.foreach(_())

  destroy().afterSpec

}