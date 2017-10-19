package play.api.deadbolt.authz

import javax.inject.{Inject, Singleton}

import org.apache.commons.lang3.StringUtils
import play.api.deadbolt.session.Session
import play.api.deadbolt.session.mgt.{DefaultSessionKey, MapSession, SessionContext, SessionManager}
import play.api.deadbolt.{Authz, Subject}
import play.api.http.SessionConfiguration
import play.mvc.Http

import scala.collection.JavaConverters._

@Singleton
class DefaultAuthz @Inject() (securityManager: SecurityManager,sessionManager: SessionManager,sessionCfg: SessionConfiguration) extends Authz{
  private val anonymous:Subject = new SimpleSubject("anonymous","anonymous",false)

  override def getSubject(request: Http.Session):Subject = {
    val session = getSession(request)
    var subject = securityManager.getSubject(session)
    if(subject == null){
      subject = anonymous
    }
    subject
  }

  override def getSession(request: Http.Session):Session = {
    val sessionId = request.get(sessionCfg.cookieName)
    if(StringUtils.isNoneEmpty(sessionId)){
      sessionManager.getSession(new DefaultSessionKey(sessionId))
    }else{
      toMapSession(request)
    }
  }

  def toMapSession(request: Http.Session):Session = new MapSession(request)

  def login(token:AuthenticationToken,request:play.mvc.Http.Session):Either[Subject,String] = {
    val subjectE = securityManager.login(token)
    if(subjectE.isLeft){
      val map:java.util.Map[String,AnyRef] = new java.util.HashMap[String,AnyRef]()
      request.asScala.map{
        case (key,value) => map.put(key,value)
      }
      val subject = subjectE.left.get
      map.put("subject_id",subject.id)
      map.put("subject_name",subject.name)
      val session = sessionManager.start(new SessionContext(map))
      request.put(sessionCfg.cookieName,session.getId.toString)

      securityManager.attachSession(subject,session)
    }
    subjectE
  }

  def logout(subject: Subject,request:play.mvc.Http.Session):Unit = {
    val session = getSession(request)
    securityManager.logout(subject,session)
    sessionManager.destroy(session)
  }

  override def encryptPassword(pwd: String):String = securityManager.encryptPassword(pwd)

}
