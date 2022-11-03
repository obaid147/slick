package com.rockwithjvm

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.utils.SimpleArrayUtils
import play.api.libs.json.{JsValue, Json}

trait CustomPostgresProfile
  extends ExPostgresProfile
  with PgArraySupport // for Arrays values
  with PgHStoreSupport // for Maps values
  with PgJsonSupport// for json values
  with PgPlayJsonSupport/* for Json values*/ {
  override def pgjson: String = "jsonb"

  override val api = CustomPostgresAPI // api is a main object with all definitions, methods, logic inside....

  object CustomPostgresAPI extends API
    with ArrayImplicits
    with HStoreImplicits
    with JsonImplicits {
    implicit val stringListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val playJsonArrayTypeMapper = new AdvancedArrayJdbcType[JsValue](
      pgjson,
      string => SimpleArrayUtils.fromString(Json.parse)(string).orNull,
      value => SimpleArrayUtils.mkString[JsValue](_.toString)(value)
    ).to(_.toList)
  }

}

object CustomPostgresProfile extends CustomPostgresProfile
