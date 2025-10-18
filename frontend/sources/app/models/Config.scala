package models

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

        implicit val writes : Writes[Config] = new Writes[Config] {
            def writes(config: Config): JsValue = {
                Json.obj(
                    "name" -> config.name,
                    "id" -> config.id,
                    "memory_mapped_registers" -> Json.obj(
                        "mtime" -> ("0x" + config.mtime.toString(16)),
                        "mtimeh" -> ("0x" + config.mtimeh.toString(16)),
                        "mtimecmp" -> ("0x" + config.mtimecmp.toString(16)),
                        "mtimecmph" -> ("0x" + config.mtimecmph.toString(16)),
                        "keyboard_ready" -> ("0x" + config.keyboard_ready.toString(16)),
                        "keyboard_data" -> ("0x" + config.keyboard_data.toString(16)),
                        "terminal_ready" -> ("0x" + config.terminal_ready.toString(16)),
                        "terminal_data" -> ("0x" + config.terminal_data.toString(16)),
                        "display" -> ("0x" + config.display.toString(16))
                    ),
                    "initial_pc" -> ("0x" + config.initial_pc.toString(16)),
                    "os_program_file" -> config.machine_program_file,
                    "user_programs" -> config.user_program_files.map(x => Map("address" -> ("0x" + x._1.toString(16)), "name" -> x._2._1, "file" -> x._2._2)),
                    "initial_memory" -> Json.obj(config.initial_memory.map {
                                            case (key, value) => ("0x" + key.toString(16)) -> Json.toJsFieldJsValueWrapper(JsString("0x" + value.toString(16)))
                                        }.toSeq: _*), //config.initial_memory.map(x => ("0x" + x._1.toString(16)) -> ("0x" + x._2.toString(16))),
                    "terminal_delay" -> config.terminal_delay.toString
                )
            }
        }

        def loadConfig(path: String): Config = {
            val string = Source.fromResource(path).mkString
            val json = Json.parse(string)
            return json.as[Config]
        }
    }