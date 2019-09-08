package scalafix.shield

import java.nio.file.Path

import scalafix.internal.reflect.ClasspathOps
import scalafix.v1.{SemanticDocument, SyntacticDocument}

import scala.meta.internal.io.PathIO
import scala.meta.internal.symtab.SymbolTable
import scala.meta.io.{AbsolutePath, Classpath}
import scala.util.Try

class ZioShieldScalafixExtension(fullClasspath: List[Path]) {

  private val classpath: Classpath =
    if (fullClasspath.isEmpty) {
      val roots = PathIO.workingDirectory :: Nil
      ClasspathOps.autoClasspath(roots)
    } else {
      Classpath(fullClasspath.map(AbsolutePath(_)))
    }

  private val symtab: SymbolTable =
    ClasspathOps.newSymbolTable(
      classpath = classpath,
      out = System.out
    )

  private val classLoader: ClassLoader =
    ClasspathOps.toOrphanClassLoader(classpath)

  def semanticDocumentFromPath(
      doc: SyntacticDocument,
      path: Path): Either[Throwable, SemanticDocument] =
    for {
      doc <- Try {
        SemanticDocument.fromPath(
          doc,
          AbsolutePath(path).toRelative(PathIO.workingDirectory),
          classLoader,
          symtab
        )
      }.toEither
    } yield doc
}