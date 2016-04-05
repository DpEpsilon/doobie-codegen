package mdmoss.doobiegen

import mdmoss.doobiegen.Runner.Target

object Code {

  case class CodegenResult(src: Seq[OutFile], tests: Seq[OutFile])

  def checkTest(o: ObjectPlan, f: FunctionDef): Block = Block(
    s"check(${o.name}.${f.name}(${f.params.map(_.`type`.arb).mkString(", ")}))"
  )

  implicit class indentString(s: String) {
    def indent(nSpaces: Int) = s.split("\n").map(p => " " * nSpaces + p).mkString("\n")
  }
}

import Code._

case class OutFile(path: String, name: String, contents: String)

sealed trait CodePart {def pp: String}

case class Import(`package`: String) extends CodePart {
  def pp = s"import ${`package`}"
}

case class FunctionDef(privatePkg: Option[String], name: String, params: Seq[FunctionParam], returnType: String, body: String) extends CodePart {
  def pp = {
    val prv = privatePkg.map(p => s"private[$p] ").getOrElse("")

    s"""${prv}def $name(${params.map { p => s"${p.name}: ${p.`type`.symbol}" }.mkString(", ")}): $returnType = {
       |${body.indent(2)}
       |}
     """.stripMargin
  }
}
case class FunctionParam(name: String, `type`: ScalaType)

case class CaseClassDef(name: String, fields: Seq[CaseClassField]) extends CodePart {
  def pp = {
    s"case class $name(${fields.map(f => s"${f.name}: ${f.`type`.symbol}").mkString(", ")})"
  }
}
case class CaseClassField(name: String, `type`: ScalaType)

case class Block(body: String) extends CodePart {
  override def pp: String = body
}