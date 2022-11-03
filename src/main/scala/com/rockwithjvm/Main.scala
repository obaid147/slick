package com.rockwithjvm

import slick.dbio.Effect
import slick.jdbc.GetResult
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

  // movies
  val shawshank = Movie(1L, "Shawshank Redemption", Some(LocalDate.of(1994, 4, 2)), 162)
  val theMatrix = Movie(2L, "The Matrix", Some(LocalDate.of(1999, 3, 31)), 145)
  val phantomMenace = Movie(3L, "Star Wars: Phantom Menace",
                      Some(LocalDate.of(1999, 5, 16)), 133)

  // actors
  val tomHank = Actor(1L, "Tom Hank")
  val julia = Actor(2L, "Julia")
  val liamNeeson = Actor(3L, "Liam Neeson")

  def demoInsertMovie(): Unit = {
    val queryDescription: FixedSqlAction[Int, NoStream, Effect.Write] = SlickTables.movieTable += theMatrix

    val futureId: Future[Int] = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(v) => println(s"The ID is:- $v")
      case Failure(v) => println(s"The error is:- $v")
    }
    Thread.sleep(10000)
  }

  def demoInsertActors(): Unit = {
    // using ++= we can add multiple actors once...
    val queryDescription = SlickTables.actorTable ++= Seq(tomHank, julia)

    val futureId = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(_) => println(s"Success....")
      case Failure(ex) => println(s"Failure, The error is:- $ex")
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
  // part1 ends... CRUD

  // part2 starts...
  def readMovieByPlainQuery(): Future[Vector[Movie]] = {
    implicit val getResultMovie: GetResult[Movie] =
      GetResult(positionResult =>
        Movie(
          positionResult.<<, //id
          positionResult.<<, //name
          Some(LocalDate.parse(positionResult.nextString())), //localDate
          positionResult.<< //lengthInMinutes
        )
      )
    val query = sql"""select * from movies."Movie"""".as[Movie] // required implicit
    Connection.db.run(query)
  }

  def multipleQueriesSingleTransaction(): Unit = {
    // insert movie and actor in same transaction... 3L
    val insertMovie = SlickTables.movieTable += phantomMenace
    val insertActor = SlickTables.actorTable += liamNeeson
    val finalQuery = DBIO.seq(insertMovie, insertActor)
    Connection.db.run(finalQuery.transactionally) // transactionally:- IF movie of actor failed, roll both back...
      .onComplete{
        case Success(_) => println("Transaction Successfully completed.")
        case Failure(ex) => println(s"Transaction Failed:- $ex")
      }
  }

  //MovieActorMapping
  def findAllActorsByMovie(movieId: Long): Future[Seq[Actor]] = {
    val joinQuery = SlickTables.movieActorMappingTable
      .filter(_.movieId === movieId)
      .join(SlickTables.actorTable)
      .on(_.actorId === _.id) // select * from MovieActorMapping m join Actor a on m.actor_id == a.id';
      .map(_._2) // map and return just actors

    Connection.db.run(joinQuery.result)
  }

  def findAllMoviesByActor(actorId: Long): Future[Seq[Movie]] = {
    val joinQuery = SlickTables.movieActorMappingTable
      .filter(_.actorId === actorId)
      .join(SlickTables.movieTable)
      .on(_.movieId === _.id)
      .map(_._2)
    Connection.db.run(joinQuery.result)
  }

  def main(args: Array[String]): Unit = {
    // demoInsertMovie()
    // demoReadAllMovies()
    // demoReadSomeMovie()
    // demoUpdate()
    // demoReadAllMovies()
    // demoDelete()
    // findMovieByName("Shawshank Redemptio")

    // part2...
    // demoInsertMovie()
    /*readMovieByPlainQuery().onComplete {
      case Success(movies) => println(s"Query successful, movies: $movies")
      case Failure(ex) => println(s"Query Failed: $ex")
    }*/

    // demoInsertActors()

    // multipleQueriesSingleTransaction()

    /*findAllActorsByMovie(4L).onComplete {
      case Failure(exception) => println(s"Failed:- $exception")
      case Success(value) => println(value)
    }*/

    findAllMoviesByActor(3L).onComplete{
      case Failure(exception) => println(s"Failed:- $exception")
      case Success(value) => println(value)
    }
    Thread.sleep(5000)
    PrivateExecutionContext.executor.shutdown()


  }
}