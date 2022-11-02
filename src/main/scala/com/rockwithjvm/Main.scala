package com.rockwithjvm

import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlAction

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main {
  val shawshank = Movie(1L, "Shawshank Redemption", Some(LocalDate.of(1994, 4, 2)), 162)

  def insertMovie(): Unit = {
    val insertQuery: FixedSqlAction[Int, NoStream, Effect.Write] = SlickTables.movieTable += shawshank

    val futureId: Future[Int] = Connection.db.run(insertQuery)

    futureId.onComplete {
      case Success(v) => println(s"The ID is:- $v")
      case Failure(v) => println(s"The error is:- $v")
    }
    Thread.sleep(10000)

  }
  def main(args: Array[String]): Unit = {
    insertMovie()
  }
}