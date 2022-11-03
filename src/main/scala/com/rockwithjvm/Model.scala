package com.rockwithjvm

import play.api.libs.json.JsValue

import java.time.LocalDate

case class Movie(id: Long, name: String, releaseDate: Option[LocalDate], lengthInMin: Int)
case class Actor(id: Long, name: String)
case class MovieActorMapping(id: Long, movie_id: Long, actor_id: Long) // actor movie mapping (JOINS)

//part4 using slick-pg_play-json and slick-pg libraries
case class MovieLocations(id: Long, movieId: Long, locations: List[String])
case class MovieProperties(id: Long, movieId: Long, properties: Map[String, String])
case class ActorDetails(id: Long, actorId: Long, personalDetails: JsValue)
object SpecialTables {

  import CustomPostgresProfile.api._
  // We need to define our own slick profile to support datastructures like List...CustomPostgresProfile.scala
  class MovieLocationsTable(tag: Tag) extends Table[MovieLocations](tag, Some("movies"), "MovieLocations") {
    def id = column[Long]("movie_location_id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie_id")
    def location = column[List[String]]("locations") // cannot simple pass implicit here, we need to change import.
    override def * =
      (id, movieId, location) <> (MovieLocations.tupled, MovieLocations.unapply)
  }

  // "API entry point"
  lazy val movieLocationsTable = TableQuery[MovieLocationsTable]
    // --------------
  class MoviePropertiesTable(tag: Tag) extends Table[MovieProperties](tag, Some("movies"), "MovieProperties") {
    def id = column[Long]("movie_location_id", O.PrimaryKey, O.AutoInc)

    def movieId = column[Long]("movie_id")

    def properties = column[Map[String, String]]("properties")

    override def * =
      (id, movieId, properties) <> (MovieProperties.tupled, MovieProperties.unapply)
  }

  // "API entry point"
  lazy val moviePropertiesTable = TableQuery[MoviePropertiesTable]

  // ----------
  class ActorDetailsTable(tag: Tag) extends Table[ActorDetails](tag, Some("movies"), "ActorDetails") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def actorId = column[Long]("actor_id")

    def personalDetails = column[JsValue]("personal_info")

    override def * =
      (id, actorId, personalDetails) <> (ActorDetails.tupled, ActorDetails.unapply)
  }

  // "API entry point"
  lazy val actorDetailsTable = TableQuery[ActorDetailsTable]

}


case class SteamingProviderMapping(id: Long, movieId: Long, streamingProvider: StreamingService.Provider)
object StreamingService extends Enumeration {
  type Provider = Value
  val Netflix = Value("Netflix")
  val Disney = Value("Disney+")
  val Prime = Value("AmazonPrime")
  val Sony = Value("SonyLiv")
}
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

  // Actor Mapping
  class ActorTable(tag: Tag) extends Table[Actor](tag, Some("movies"), "Actor") {
    def id = column[Long]("actor_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    override def * = (id, name) <> (Actor.tupled, Actor.unapply)
    // <> is a bidirectional mapping function between a tuple and an Actor
    // Actor.tupled is a constructor which constructs and actor out of tuple & Actor.unapply does the reverse.
  }

  lazy val actorTable = TableQuery[ActorTable]

  // Actor Movie Mapping
  class MovieActorMappingTable (tag: Tag)
  extends Table[MovieActorMapping](tag, Some("movies"), "MovieActorMapping") {
    def id = column[Long]("movie_actor_id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie_id")
    def actorId = column[Long]("actor_id")
    override def * = (id, movieId, actorId) <> (MovieActorMapping.tupled, MovieActorMapping.unapply)
  }
  lazy val movieActorMappingTable = TableQuery[MovieActorMappingTable]
  // insert into movies."MovieActorMapping" values(1, 4, 3); in terminal and the goto Main.scala for joins...


  // streaming service mapping
  class StreamingProviderMappingTable(tag: Tag) extends
    Table[SteamingProviderMapping](tag, Some("movies"), "StreamingProviderMapping") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie_id")

    implicit val providerMapper = MappedColumnType.base[StreamingService.Provider, String](
      provide => provide.toString,
      string => StreamingService.withName(string)
    ) // implicit for streamingProvider
    def streamingProvider = column[StreamingService.Provider]("streaming_provider")
    override def * =
      (id, movieId, streamingProvider)<>(SteamingProviderMapping.tupled, SteamingProviderMapping.unapply)
  }
  lazy val streamingProviderMappingTable = TableQuery[StreamingProviderMappingTable]

  //table Generation Script  ->>> This is to generate tables for init-script.sql
  val tables = List(movieTable, actorTable, movieActorMappingTable, streamingProviderMappingTable)
  val ddl = tables.map(_.schema).reduce(_ ++ _)
}

object TableDefinitionGenerator {
  def main(args: Array[String]): Unit = { // RUN THIS and copy the generate table
    println(SlickTables.ddl.createIfNotExistsStatements.mkString(";\n"))
  }
}


