package models

import play.api.libs.json._

case class Program(
    name: String,
    baseAddress: BigInt, 
    instructions: Map[BigInt, BigInt], 
    assembly: Map[BigInt, String],
    labels: Map[BigInt, String]
) {
    def getInstruction(address: BigInt) : Option[BigInt] = {
        if (instructions.keySet.contains(address)) {
            return Option(instructions(address))
        } else {
            return Option.empty
        }
    }
}

object Program {
    implicit val reads : Reads[Program] = new Reads[Program] {
        def reads(json: JsValue): JsResult[Program] = {
            val name = (json \ "name").as[String]
            val baseAddress = BigInt((json \ "base_address").as[Long])
            val instructions = (json \ "instructions").as[Map[Long, Long]].map(x => BigInt(x._1) -> BigInt(x._2))
            val assembly = (json \ "assembly").as[Map[Long, String]].map(x => BigInt(x._1) -> x._2)
            val labels = (json \ "labels").as[Map[Long, String]].map(x => BigInt(x._1) -> x._2)
            JsSuccess(new Program(name, baseAddress, instructions, assembly, labels))
        }
    }

    implicit val writes : Writes[Program] = new Writes[Program] {
        def writes(program: Program): JsValue = {
            Json.obj(
                "name" -> program.name,
                "base_address" -> Json.toJson(program.baseAddress.longValue),
                "instructions" -> Json.toJson(program.instructions.map(x => x._1.longValue -> x._2.longValue)),
                "assembly" -> Json.toJson(program.assembly.map(x => x._1.longValue -> x._2)),
                "labels" -> Json.toJson(program.labels.map(x => x._1.longValue -> x._2))
            )
        }
    }
}