package com.rockwithjvm

import java.time.LocalDate

case class Movie(id: Long, name: String, releaseDate: Option[LocalDate], lengthInMin: Int)
case class Actor(id: Long, name: String)

object SlickTables {

  import slick.jdbc.PostgresProfile.api._
  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies"), "Movie") {
    def id = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def releaseDate = column[Option[LocalDate]]("release_date")
    def lengthInMin = column[Int]("length_in_min")

    // * is a mapping function to the case class
    override def * = (id, name, releaseDate, lengthInMin) <> (Movie.tupled, Movie.unapply)
  }

  // "API entry point"
  lazy val movieTable = TableQuery[MovieTable]

  class ActorTable(tag: Tag) extends Table[Actor](tag, Some("movies"), "Actor") {
    def id = column[Long]("actor_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    override def * = (id, name) <> (Actor.tupled, Actor.unapply)
    // <> is a bidirectional mapping function between a tuple and an Actor
    // Actor.tupled is a constructor which constructs and actor out of tuple & Actor.unapply does the reverse.
  }

  lazy val actorTable = TableQuery[ActorTable]

}
