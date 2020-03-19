package com.github.radium226.example

import java.time.LocalDate

import cats._
import cats.data._
import cats.implicits._

import scala.util._

object CatsWithStreamExample extends App {

  def count(dayDate: LocalDate): Try[Long] = {
    println(s" --> ${dayDate}")
    dayDate match {

      case dayDate if dayDate == LocalDate.now().minusDays(1) =>
        Failure(new Exception("Va te faire enculer"))

      case dayDate if dayDate == LocalDate.now().minusDays(2) =>
        Success(32)

      case _ =>
        Success(0)
    }
  }

  val dayDates = Stream.iterate(LocalDate.now)(_.minusDays(1))

  dayDates
    .map({ dayDate =>
      count(dayDate).map((dayDate, _))
    })
    .collectFirst({
      case Success((dayDate, count)) if count != 0 =>
        Success(dayDate)

      case Failure(exception) =>
        Failure(exception)
    })
    .getOrElse(Failure(new NoSuchElementException)) match {
      case Success(actualDayDate) =>
        println(s"actualDayDate=${actualDayDate}")

      case Failure(e) =>
        e.printStackTrace(System.err)
    }
}
