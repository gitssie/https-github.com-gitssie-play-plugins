package play.api.deadbolt

trait Subject {

  def id:String

  def name:String

  def nickName:String

  def idAsInt:Int

  def idAsLong:Long

  def roles: List[Role]

  def permissions: List[Permission]

  def toDelegate[T] : T

  def isAuthenticated: Boolean
}

