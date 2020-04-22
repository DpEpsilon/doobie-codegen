package mdmoss.doobiegen.db.pg_catalog.gen

/* Todo handle imports better */
import doobie.imports._
import java.sql.{Date, Timestamp, Time}
import java.util.UUID
import java.time.{LocalDate, LocalDateTime}
import scalaz._, Scalaz._

import doobie.postgres.imports._

object PgDepend extends PgDepend {

  case class Row(
    objsubid: Int,
    refobjsubid: Int
  ) {
    def toShape: Shape = Shape.NoDefaults(objsubid, refobjsubid)
  }

  object Row {
    val ColumnsFragment: Fragment = fr"objsubid, refobjsubid"
    def aliasedColumnsFragment(a: String): Fragment = Fragment.const0(a) ++ fr".objsubid, " ++ Fragment.const0(a) ++ fr".refobjsubid"
  }

  case class Shape(objsubid: Int, refobjsubid: Int)

  object Shape {
    def NoDefaults(objsubid: Int, refobjsubid: Int): Shape = Shape(objsubid, refobjsubid)
  }

    private val zippedRowComposite = Composite.fromMeta(doobie.util.meta.Meta.IntMeta)
    .zip(Composite.fromMeta(doobie.util.meta.Meta.IntMeta))

    implicit def RowComposite: Composite[Row] = {
      zippedRowComposite.xmap(
        t => Row(t._1,
    t._2),
        (row) => (row.objsubid, (row.refobjsubid))
      )
    }

    private val zippedShapeComposite = Composite.fromMeta(doobie.util.meta.Meta.IntMeta)
    .zip(Composite.fromMeta(doobie.util.meta.Meta.IntMeta))

    implicit def ShapeComposite: Composite[Shape] = {
      zippedShapeComposite.xmap(
        t => Shape(t._1,
    t._2),
        (row) => (row.objsubid, (row.refobjsubid))
      )
    }

}
trait PgDepend {
  import PgDepend._

  def create(objsubid: Int, refobjsubid: Int): ConnectionIO[Row] = {
    create(Shape(objsubid, refobjsubid))
  }

  def createVoid(objsubid: Int, refobjsubid: Int): ConnectionIO[Unit] = {
    createVoid(Shape(objsubid, refobjsubid))
  }

  private[gen] def insertMany(values: List[mdmoss.doobiegen.db.pg_catalog.gen.PgDepend.Shape]): Update[Shape] = {
    val sql = "INSERT INTO pg_catalog.pg_depend (objsubid, refobjsubid) VALUES (?, ?)"
    Update[Shape](sql)
  }

  private[gen] def createManyP(values: List[mdmoss.doobiegen.db.pg_catalog.gen.PgDepend.Shape]): scalaz.stream.Process[ConnectionIO, Row] = {
    insertMany(values).updateManyWithGeneratedKeys[Row]("objsubid", "refobjsubid")(values)
  }

  def createMany(values: List[mdmoss.doobiegen.db.pg_catalog.gen.PgDepend.Shape]): ConnectionIO[List[Row]] = {
    if (values.nonEmpty) createManyP(values).runLog.map(_.toList) else List.empty.point[ConnectionIO]
  }

  def createManyVoid(values: List[mdmoss.doobiegen.db.pg_catalog.gen.PgDepend.Shape]): ConnectionIO[Unit] = {
    if (values.nonEmpty) insertMany(values).updateMany[List](values).map(_ => ()) else ().point[ConnectionIO]
  }

  def create(shape: mdmoss.doobiegen.db.pg_catalog.gen.PgDepend.Shape): ConnectionIO[Row] = {
    createMany(shape :: Nil).map(_.head)
  }

  def createVoid(shape: mdmoss.doobiegen.db.pg_catalog.gen.PgDepend.Shape): ConnectionIO[Unit] = {
    createManyVoid(shape :: Nil)
  }

  private[gen] def allInner(offset: Long, limit: Long): Query0[Row] = {
    (sql"""
      SELECT """ ++ Row.ColumnsFragment ++ sql"""
      FROM pg_catalog.pg_depend
      OFFSET $offset
      LIMIT $limit
    """).query[Row]
  }

  def all(offset: Long, limit: Long): ConnectionIO[List[Row]] = {
    allInner(offset, limit).list
  }

  def allUnbounded(): ConnectionIO[List[Row]] = {
    allInner(0, 9223372036854775807L).list
  }

  private[gen] def countInner(): Query0[Long] = {
    sql"""
      SELECT COUNT(*)
      FROM pg_catalog.pg_depend
    """.query[Long]
  }
  def count(): ConnectionIO[Long] = {
    countInner().unique
  }

}
