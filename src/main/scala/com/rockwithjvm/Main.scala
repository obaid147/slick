package com.rockwithjvm

import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlAction

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object PrivateExecutionContext {
  val executor = Executors.newFixedThreadPool(4)
  implicit val ex: ExecutionContext = ExecutionContext.fromExecutorService(executor)
}

object Main {
  import PrivateExecutionContext._

  val shawshank = Movie(1L, "Shawshank Redemption", Some(LocalDate.of(1994, 4, 2)), 162)
  val theMatrix = Movie(2L, "The Matrix", Some(LocalDate.of(1999, 3, 31)), 145)

  def demoInsertMovie(): Unit = {
    val queryDescription: FixedSqlAction[Int, NoStream, Effect.Write] = SlickTables.movieTable += theMatrix

    val futureId: Future[Int] = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(v) => println(s"The ID is:- $v")
      case Failure(v) => println(s"The error is:- $v")
    }
    Thread.sleep(10000)
  }
  
  def main(args: Array[String]): Unit = {
    demoInsertMovie()
  }
}