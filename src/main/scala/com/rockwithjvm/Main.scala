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

  def demoReadAllMovies(): Unit = {
    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.result) // select * from tableName
    resultFuture.onComplete {
      case Success(movies) => println(s"Fetched: ${movies.mkString(", ")}")
      case Failure(ex) => println(s"Fetching failed:- $ex")
    }
    Thread.sleep(10000)
  }

  def demoReadSomeMovie(): Unit = {
    val resultFuture: Future[Seq[Movie]] =
      Connection.db.run(SlickTables.movieTable.filter(_.name.like("%Matrix%")).result)
                        // select * from tableName where name like "Matrix"
    resultFuture.onComplete {
      case Success(movies) => println(s"Fetched: ${movies.mkString(", ")}")
      case Failure(ex) => println(s"Fetching failed:- $ex")
    }
    Thread.sleep(10000)
  }

  def findMovieByName(name: String): Unit = {
    val res = Connection.db.run(SlickTables.movieTable.filter(_.name === name).result.headOption)
    res.onComplete{
      case Success(movie) => println(s"Fetched Movie:- $movie")
      //case Failure(ex) => println(s"Fetching failed:- $ex")
    }
    Thread.sleep(10000)
  }

  def demoUpdate(): Unit = {
    val queryDescriptor = SlickTables.movieTable.filter(_.id === 1L).update(shawshank.copy(lengthInMin = 150))
    val futureId: Future[Int] = Connection.db.run(queryDescriptor)
    futureId.onComplete{
      case Success(id) => println(s"Updated movie with id: $id")
      case Failure(ex) => println(s"Update failed:- $ex")
    }
    Thread.sleep(10000)
  }

  def demoDelete(): Unit = {
    val deleteQuery: Future[Int] = Connection.db.run(SlickTables.movieTable.filter(_.name.like("%Matrix%")).delete)

    deleteQuery.onComplete{
      case Success(id) => println(s"id: $id deleted")
      case Failure(ex) => println(s"Deletion failed $ex")
    }
    Thread.sleep(10000)
  }


  def main(args: Array[String]): Unit = {
    //    demoInsertMovie()
    // demoReadAllMovies()
    // demoReadSomeMovie()
    // demoUpdate()
    // demoReadAllMovies()
    //demoDelete()
    findMovieByName("Shawshank Redemptio")
  }
}