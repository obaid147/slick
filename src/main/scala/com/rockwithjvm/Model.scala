package com.rockwithjvm

import slick.lifted.ProvenShape
import java.time.LocalDate

case class Movie(
                  id: Long,
                  name: String,
                  releaseDate: Option[LocalDate],
                  lengthInMin: Int
                )


object SlickTables {

  import slick.jdbc.PostgresProfile.api._
  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies"), "Movie") {


    def id = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def releaseDate = column[Option[LocalDate]]("release_date")
    def lengthInMin = column[Int]("length_in_min")

    // * is a mapping function to the case class
    override def * : ProvenShape[Movie] = (id, name, releaseDate, lengthInMin) <> (Movie.tupled, Movie.unapply)
  }

  // "API entry point"
  lazy val movieTable = TableQuery[MovieTable]

}
