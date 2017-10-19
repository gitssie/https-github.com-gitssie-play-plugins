package play.api.deadbolt.session

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import com.typesafe.config.Config
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.deadbolt.session.backend.{CacheApiDao, MemorySessionDAO}
import play.api.deadbolt.session.mgt._
import play.api.inject.{Injector, SimpleModule}
import play.cache.NamedCacheImpl
import play.utils.Reflect

import scala.concurrent.duration.Duration


/**
  * play.deadbolt.session{
  *   backend="memory|cacheName"
  *   factory=""
  *   timeout=""
  *   prefix=""
  * }
  */
@Singleton
class SessionModule @Inject()(inject:Injector) extends SimpleModule((env, conf) => {

  conf.getOptional[Config]("play.deadbolt.session").map(conf => {
    val cfg = Configuration(conf)

    val configs = new Configs()
    configs.sessionPrefix = cfg.getOptional[String]("prefix").getOrElse("session.")
    configs.globalSessionTimeout = cfg.getOptional[Duration]("timeout").getOrElse(Duration(1,TimeUnit.HOURS)).toMillis

    val backend: SessionDAO = cfg.getOptional[String]("backend").map{
      case name if name == "memory" => new MemorySessionDAO()
      case name => {
        val namedCache = new NamedCacheImpl(name)
        val cacheApiKey = play.api.inject.bind[AsyncCacheApi].qualifiedWith(namedCache)
        val cachedApi = inject.instanceOf(cacheApiKey)
        new CacheApiDao(configs, cachedApi)
      }
    }.getOrElse(new MemorySessionDAO())

    val factoryCls = cfg.getOptional[String]("factory").map(fct => Reflect.getClass[SessionFactory](fct,env.classLoader)).getOrElse(classOf[SimpleSessionFactory])
    val factory:SessionFactory = Reflect.createInstance(factoryCls)

    val idGeneratorCls = cfg.getOptional[String]("idGenerator").map(fct => Reflect.getClass[SessionIdGenerator](fct,env.classLoader)).getOrElse(classOf[RandomSessionIdGenerator])
    val idGenerator:SessionIdGenerator = Reflect.createInstance(idGeneratorCls)

    val sessionManager = new DefaultSessionManager(configs,backend,factory,idGenerator)

    Seq(
      play.api.inject.bind[SessionManager].toInstance(sessionManager)
    )
  }).getOrElse(Seq.empty)
})

