package models

import play.api.libs.json._

case class ValueUpdate[T](
    val oldValue: T,
    val newValue: T
)

object ValueUpdate {

    implicit def reads[T](implicit fmt: Reads[T]): Reads[ValueUpdate[T]] = new Reads[ValueUpdate[T]] {
        def reads(json: JsValue): JsResult[ValueUpdate[T]] = {
            val oldValue = (json \ "old_value").as[T]
            val newValue = (json \ "new_value").as[T]
            JsSuccess(ValueUpdate(oldValue, newValue))
        }
    }

    implicit def writes[T](implicit fmt: Writes[T]): Writes[ValueUpdate[T]] = new Writes[ValueUpdate[T]] {
        def writes(valueUpdate: ValueUpdate[T]): JsValue = {
            Json.obj(
                "old_value" -> Json.toJson(valueUpdate.oldValue),
                "new_value" -> Json.toJson(valueUpdate.newValue)
            )
        }
    }
}