package com.github.radium226.bnpp.bddf

import scala.util._

object Example extends App {

  import cats._
  import cats.data._
  import cats.implicits._

  case class Input(words: String)

  case class Output(word: String)

  type Result[E] = Either[String, E]

  object Result {

    def succeed[E](e: E): Result[E] = {
      Right(e)
    }

    def failed[E](cause: String): Result[E] = {
      Left(cause)
    }

  }

  type Stage[E] = EitherT[List, String, E]

  object Stage {

    def apply[E](value: List[Result[E]]): Stage[E] = {
      EitherT(value)
    }

    def failed[E](cause: String): Stage[E] = {
      EitherT[List, String, E](List(Result.failed(cause)))
    }

    def succeed[E](elements: List[E]): Stage[E] = {
      apply(elements.map(Result.succeed))
    }

  }

  /*implicit class StageOps[A](fa: Stage[A]) {

    def flatMap[B](f: A => Stage[B]): Stage[B] = {
      fa.value.foldLeft[Stage[B]](Stage(List.empty[Result[B]])) { (stage: Stage[B], result: Result[A]) =>
        //println(s"stage = ${stage}")
        result match {
          case Left(cause) =>
            Stage(stage.value :+ Left(cause))

          case Right(a) =>
            Stage(stage.value ++ f(a).value)
        }
      }
    }
  }*/

  def splitToWords(words: String): Stage[String] = {
    Stage.succeed(words.split(" ").toList)
  }

  def splitToCharacters(word: String): Stage[String] = {
    Stage.succeed[String](word.toList.map(_.toString))
  }

  def validateWord(word: String): Stage[String] = {
    if (word.matches("[0-9]+")) Stage.failed(s"${word} is not a word! ") else Stage.succeed(List(word))
  }

  val stage = for {
    input       <- Stage.succeed[Input](List(Input("Hello 11 Comment ça va"), Input("Yo ça gaze")))
    words        = input.words
    word        <- splitToWords(words)
    _           <- validateWord(word)
    char        <- splitToCharacters(word)
    output       = Output(char)
  } yield output

  println(stage)

}
