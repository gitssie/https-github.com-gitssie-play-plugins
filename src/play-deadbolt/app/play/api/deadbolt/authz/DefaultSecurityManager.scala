package play.api.deadbolt.authz
import java.util.concurrent.TimeUnit

import org.apache.commons.lang3.StringUtils
import play.api.cache.{AsyncCacheApi, SyncCacheApi}
import play.api.deadbolt.realm.Realm
import play.api.deadbolt.session.Session
import play.api.deadbolt.{Permission, Role, Subject}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class DefaultSecurityManager(realm:Realm,passwordService:PasswordService,cachedApi:SyncCacheApi) extends SecurityManager{

  private lazy val allowAllMatch:PasswordService = new PasswordService {
    override def encryptPassword(plaintextPassword: AnyRef): String = passwordService.encryptPassword(plaintextPassword)
    override def passwordsMatch(submittedPlaintext: AnyRef, encrypted: String): Boolean = true
    override def decryptPassword(encrypted: String): String = passwordService.decryptPassword(encrypted)
  }

  def createSubject(subject: Subject, roles: List[Role], permissions: List[Permission]): Subject = new SimpleSubject(subject,roles,permissions)

  /**
    * authentication subject and local cached and put in session
    */
  override def login(token: AuthenticationToken): Either[Subject, String] = login(token,passwordService)

  def login(token: AuthenticationToken,passwordService: PasswordService): Either[Subject, String] = {
    val subjectE = realm.getAuthenticationInfo(token,passwordService)
    if(subjectE.isLeft){
      val subject = subjectE.left.get
      val roles = realm.getRoles(subject)
      val permissions = realm.getPermissions(subject)
      val newSubject:Subject = createSubject(subject,roles,permissions)
      Left(newSubject)
    }else{
      subjectE
    }
  }

  /**
    * clean cached subject
    */
  override def logout(subject: Subject,s: Session): Unit = {
    cachedApi.remove(classOf[SecurityManager].getSimpleName + "." + s.getId)
  }

  override def encryptPassword(pwd: String): String = passwordService.encryptPassword(pwd)

  override def getSubject(session: Session):Subject = Option(session).filter(s => s.getId != null && s.getAttribute("subject_id") != null && s.getAttribute("subject_name") != null).map(s =>{
    var subject:Subject = cachedApi.get[Subject](classOf[SecurityManager].getSimpleName + "." + s.getId).getOrElse(null)
    if(subject == null) {
      val token = new UsernamePasswordToken(s.getAttribute("subject_name").toString, s.getAttribute("subject_id").toString)
      val subjectE = login(token, allowAllMatch)
      if (subjectE.isLeft) {
          subject = subjectE.left.get
      }
    }
    subject
  }).getOrElse(null)

  override def attachSession(subject: Subject, session: Session): Unit = Option(session).filter(s => s.getId != null && s.getAttribute("subject_id") != null && s.getAttribute("subject_name") != null).map(s =>{
    cachedApi.set(classOf[SecurityManager].getSimpleName + "." + s.getId,subject,Duration(s.getTimeout,TimeUnit.MILLISECONDS))
  })
}
