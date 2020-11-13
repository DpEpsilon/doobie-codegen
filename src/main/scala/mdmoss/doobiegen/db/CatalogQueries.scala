package mdmoss.doobiegen.db

import doobie.imports._
import doobie.postgres.imports._

object CatalogQueries {
  case class Enum(typeName: String, values: Array[String])

  def getEnums(): ConnectionIO[List[Enum]] = {
    getEnumsInner.list
  }

  private[db] def getEnumsInner: Query0[Enum] = {
    sql"""
      WITH enum_values AS (
        SELECT enumtypid, array_agg(enumlabel ORDER BY enumsortorder) AS values
        FROM pg_enum
        GROUP BY enumtypid
      )
      SELECT t.typname, e.values
      FROM pg_type t
        JOIN enum_values e ON e.enumtypid = t.oid
    """.query[Enum]
  }
}
