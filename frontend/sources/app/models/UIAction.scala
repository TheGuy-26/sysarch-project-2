package models

import play.api.libs.json._

case class UIAction(action: String, delay: Int, cycles: Int) {
    def this(action: String) = this(action, 0, 1)
}

object UIAction {

    implicit val reads : Reads[UIAction] = new Reads[UIAction] {
        def reads(json: JsValue): JsResult[UIAction] = {
            val action = (json \ "action").as[String]
            val delay = (json \ "delay").asOpt[Int].getOrElse(0)
            val cycles = (json \ "cycles").asOpt[Int].getOrElse(1)
            JsSuccess(UIAction(action, delay, cycles))
        }
    }

    implicit val writes : Writes[UIAction] = new Writes[UIAction] {
        def writes(action: UIAction): JsValue = {
            Json.obj(
                "action" -> Json.toJson(action.action),
                "delay" -> Json.toJson(action.delay),
                "cycles" -> Json.toJson(action.cycles)
            )
        }
    }
}