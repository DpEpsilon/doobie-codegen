package mdmoss.doobiegen

import java.io.{File, PrintWriter}
import java.nio.file.Paths

import mdmoss.doobiegen.output.SourceWriter
import org.parboiled2.ParseError

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

object Runner {

  case class TestDatabase(driver: String, url: String, username: String, password: String)
  case class Target(
    schemaDir: String,
    testDb: TestDatabase,
    src: String,
    `package`: String
  ) {

    def enclosingPackage = `package`.split('.').reverse.headOption
  }

  val Default = Target(
    schemaDir = "sql/",
    TestDatabase(
      "org.postgresql.Driver",
      "jdbc:postgresql:gen",
      "test",
      "test"
    ),
    src = "out/src",
    `package` = "mdmoss.doobiegen.db"
  )

  def run(target: Target) = {
    /* We could do something more intelligent here, but this works for now */
    val sql = {
      import scala.sys.process._
      val buffer = new ListBuffer[String]()
      val proc = ("bash" :: "-c" :: s"cat ${target.schemaDir}*.sql" :: Nil).run(ProcessLogger(s => buffer.append(s)))
      proc.exitValue()
      buffer.mkString("\n")
    }

    /* We need to clean the input a little */
    import ParserUtils.SqlStringHelpers
    val cleanedAndSplit = sql
      .stripComments
      .splitOnUnescapedSemicolons
      .map(_.trim)

    val parsers = cleanedAndSplit.map(new SqlStatementParser(_))

    parsers.foreach { s =>
      println(Seperator)
      println(s.input.sliceString(0, s.input.length))
      s.StatementLine.run() match {
        case r@Success(_) => println(r)
        case r@Failure(f) => f match {
          case e@ParseError(_, _, _) => println(s.formatError(e))
            throw f
        }
      }
    }

    val statements = parsers.flatMap(_.StatementLine.run().toOption)

    println(Seperator)

    if (statements.length != parsers.length) throw new Throwable("Failed parsing. Exiting.")

    val model = statements.foldLeft(DbModel.empty)(DbModel.update)

    /*    model.tables.foreach(println)
    println(seperator)*/

    val analysis = new Analysis(model, target)

    model.tables.foreach { t =>
      /*      println(seperator)
      println(t.ref.fullName)
      println(analysis.targetPackage(t))
      println(analysis.targetObject(t))
      println(analysis.privateScope(t))
      println(analysis.pkNewType(t))
      println(analysis.rowNewType(t))
      println(analysis.insert(t))
      println(analysis.insert(t))*/
    }


    val generator = new Generator(analysis)

    val files = generator.gen

    cleanOldGenDirectories(Paths.get(target.src), files)
    SourceWriter.write(Paths.get(target.src), files)
  }

  def cleanOldGenDirectories(sourceRoot: java.nio.file.Path, files: Seq[output.File]): Unit = {
    files.foreach { f =>
      val mainOrTest = if (f.isTest) "test" else "main"

      val destDir = Paths.get(sourceRoot.toString, List(mainOrTest, "scala") ++ f.`package`.split('.'):_*)

      /* This is very heavy-handed. Todo make this nicer */

      /* Step one - go up a level, descend a level */
      destDir.toFile.getParentFile.listFiles()
        .filter(_.isDirectory)
        .flatMap(_.listFiles())
        .filter(_.isDirectory)
        .filter(_.toPath.endsWith("gen"))
        .foreach(delete)

      /* Step two - go up a level */
      destDir.toFile.getParentFile.listFiles()
        .filter(_.isDirectory)
        .filter(_.toPath.endsWith("gen"))
        .foreach(delete)
    }
  }

  def delete(file: File): Unit = {
    if (file.isDirectory) {
      file.listFiles().foreach(delete)
      file.delete()
    } else {
      file.delete()
    }
  }

  def main(args: Array[String]) = {
    run(Default)
  }

  val Seperator = "*" * 80
}
