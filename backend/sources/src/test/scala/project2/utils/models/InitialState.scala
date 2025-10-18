package project2.utils.models

import play.api.libs.json._

case class InitialState(
    machine_program: Program, 
    user_programs: List[Program], 
    registers: Map[Int, BigInt], 
    csrs: Map[BigInt, BigInt],
    memory: Map[BigInt, BigInt],
    pc: BigInt
) {
    def getInstruction(address: BigInt) = {
        machine_program.getInstruction(address).orElse {
            user_programs
            .map(_.getInstruction(address))
            .find(_.isDefined)
            .flatten
        }.getOrElse(
            throw new NoSuchElementException(f"No instruction found at address 0x${address.toString(16)} in machine or user programs")
        )
    }

    def getMemoryValue(address: BigInt) = {
        if (memory.keySet.contains(address)) {
            Option(memory(address))
        } else {
            Option.empty
        }
          
    }

    def getRegisterValue(address: Int) = {
        if (registers.keySet.contains(address)) {
            Option(registers(address))
        } else {
            Option.empty
        }
          
    }

    def getCSRValue(address: BigInt) = {
        if (csrs.keySet.contains(address)) {
            Option(csrs(address))
        } else {
            Option.empty
        }
          
    }
}

object InitialState {
    implicit val reads : Reads[InitialState] = new Reads[InitialState] {
        def reads(json: JsValue): JsResult[InitialState] = {
            val machine_program = (json \ "machine_program").as[Program]
            val user_programs = (json \ "user_programs").as[List[Program]]
            val registers = (json \ "registers").as[Map[Int, Long]].map(x => x._1 -> BigInt(x._2))
            val csrs = (json \ "csrs").as[Map[Long, Long]].map(x => BigInt(x._1) -> BigInt(x._2))
            val memory = (json \ "memory").as[Map[Long, Long]].map(x => BigInt(x._1) -> BigInt(x._2))
            val pc = BigInt((json \ "pc").as[Long])
            JsSuccess(new InitialState(machine_program, user_programs, registers, csrs, memory, pc))
        }
    }

    implicit val writes : Writes[InitialState] = new Writes[InitialState] {
        def writes(initialState: InitialState): JsValue = {
            Json.obj(
                "machine_program" -> Json.toJson(initialState.machine_program),
                "user_programs" -> Json.toJson(initialState.user_programs),
                "registers" -> Json.toJson(initialState.registers.map(x => x._1 -> x._2.longValue)),
                "csrs" -> Json.toJson(initialState.csrs.map(x => x._1.longValue -> x._2.longValue)),
                "memory" -> Json.toJson(initialState.memory.map(x => x._1.longValue -> x._2.longValue)),
                "pc" -> Json.toJson(initialState.pc.longValue)
            )
        }
    }
}