package project2.utils.models

import play.api.libs.json._
import scala.io.Source

case class Config(
        name: String,
        id: String,
        mtime: BigInt,
        mtimeh: BigInt,
        mtimecmp: BigInt,
        mtimecmph: BigInt,
        keyboard_ready: BigInt,
        keyboard_data: BigInt,
        terminal_ready: BigInt,
        terminal_data: BigInt,
        display: BigInt,
        initial_pc: BigInt,
        machine_program_file: String,
        user_program_files: Map[BigInt, (String, String)],
        initial_memory: Map[BigInt, BigInt],
        terminal_delay: BigInt
    ) {
    }
    object Config {
        implicit val reads : Reads[Config] = new Reads[Config] {
            def reads(json: JsValue): JsResult[Config] = {
                JsSuccess(new Config(
                    (json \ "name").as[String],
                    (json \ "id").as[String],
                    BigInt((json \ "memory_mapped_registers" \ "mtime").as[String].substring(2), 16),
                    BigInt((json \ "memory_mapped_registers" \ "mtimeh").as[String].substring(2), 16),
                    BigInt((json \ "memory_mapped_registers" \ "mtimecmp").as[String].substring(2), 16),
                    BigInt((json \ "memory_mapped_registers" \ "mtimecmph").as[String].substring(2), 16),
                    BigInt((json \ "memory_mapped_registers" \ "keyboard_ready").as[String].substring(2), 16),
                    BigInt((json \ "memory_mapped_registers" \ "keyboard_data").as[String].substring(2), 16),
                    BigInt((json \ "memory_mapped_registers" \ "terminal_ready").as[String].substring(2), 16),
                    BigInt((json \ "memory_mapped_registers" \ "terminal_data").as[String].substring(2), 16),
                    BigInt((json \ "memory_mapped_registers" \ "display").as[String].substring(2), 16),
                    BigInt((json \ "initial_pc").as[String].substring(2), 16),
                    (json \ "os_program_file").as[String],
                    (json \ "user_programs").as[Seq[Map[String, String]]].map(x => BigInt(x("address").substring(2), 16) -> (x("name"), x("file"))).toMap,
                    (json \ "initial_memory").as[Map[String, String]].map(x => BigInt(x._1.substring(2), 16) -> BigInt(x._2.substring(2), 16)),
                    BigInt((json \ "terminal_delay").as[String])
                ))
            }
        }

        def loadConfig(path: String): Config = {
            val string = Source.fromResource(path).mkString
            val json = Json.parse(string)
            return json.as[Config]
        }
    }