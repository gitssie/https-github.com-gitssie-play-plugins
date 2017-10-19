package play.api.deadbolt.authz

import play.api.deadbolt.{Permission, Role, Subject}

class SimpleSubject(idAsStr:String,nameStr:String,nickNameStr:String,rs:List[Role],ps:List[Permission],isAuth:Boolean) extends Subject {

  lazy val delegate = new DeadboltSubject(this)

  def this(idAsStr:String,nameStr:String,isAuth:Boolean) = this(idAsStr,nameStr,nameStr,List.empty,List.empty,isAuth)

  def this(idAsStr:String,isAuth:Boolean) = this(idAsStr,idAsStr,isAuth)

  def this(subject:Subject,rs:List[Role],ps:List[Permission]) = this(subject.id,subject.name,subject.nickName,rs,ps,true)

  override def id = idAsStr

  override def idAsInt = idAsStr.toInt

  override def idAsLong = idAsStr.toLong

  override def roles = rs

  override def permissions = ps

  override def name = nameStr

  override def nickName = nickNameStr

  override def toDelegate[T]: T = delegate.asInstanceOf[T]

  override def isAuthenticated = isAuth
}
