package play.api.routes

import play.api.http.HttpErrorHandler
import play.api.inject.Module
import play.api.{Configuration, Environment}

class RouterModule extends Module {
  override final def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[play.api.routing.Router].to(classOf[Router])
  )
}