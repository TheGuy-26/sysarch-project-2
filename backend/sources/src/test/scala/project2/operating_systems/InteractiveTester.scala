package project2.operating_systems

import chisel3._
import chiseltest._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source
import scala.collection.mutable

import sttp.client4.quick._
import sttp.client4.Response
import play.api.libs.json._

import RISCV.utils.assembler._

import project2._
import RISCV.model._
import project2.utils.models.{ValueUpdate, InitialState, Program, Config, StateUpdate, State}

trait TesterBase extends AnyFlatSpec with ChiselScalatestTester with Matchers {

    def frontends: mutable.Queue[TestFrontend] = mutable.Queue(new UIFrontend())
    var frontend: TestFrontend = frontends.dequeue()

    def getCategory(): String = "The UI"
    def getName(): String = "allow you to control the simulation interactively"
    def getImmatrikulationNumber(): BigInt = BigInt(0)

    def loadProgram(name: String, path: String, base: BigInt, config: Config): Program = {
        val source = Source.fromFile(path)
        val string = try source.mkString finally source.close()
        var labels = Map[String, String](
            "mtime" -> config.mtime.toString(16), 
            "mtimeh" -> config.mtimeh.toString(16), 
            "mtimecmp" -> config.mtimecmp.toString(16), 
            "mtimecmph" -> config.mtimecmph.toString(16), 
            "keyboard_ready" -> config.keyboard_ready.toString(16), 
            "keyboard_data" -> config.keyboard_data.toString(16), 
            "terminal_ready" -> config.terminal_ready.toString(16), 
            "terminal_data" -> config.terminal_data.toString(16),
            "display" -> config.display.toString(16)
        )
        val user_labels = config.user_program_files.map(x => x._2._1 -> x._1.toString(16)).toMap
        labels = labels ++ user_labels
        val (assembled, assembled_labels) = RISCVAssembler.mappingFromString(string, base, labels)
        val instructions = assembled.map(x => x._1 -> x._2._2).toMap
        val assembly = assembled.map(x => x._1 -> x._2._1).toMap
        val program_labels = assembled_labels.map(x => BigInt(x._2, 16) -> x._1)
        return new Program(
            name,
            base,
            instructions,
            assembly,
            program_labels
        )
    }

    def loadInitialState(dut: MachineModeCoreTop, config: Config): InitialState = {
        return new InitialState(
            loadProgram("OS", config.machine_program_file, config.initial_pc, config),
            config.user_program_files.map[Program](x => loadProgram(x._2._1, x._2._2, x._1, config)).toList,
            Map((0 to 31).map(i => i -> BigInt(0)): _*),
            getCSRValues(dut),
            config.initial_memory + (config.mtime -> BigInt(10)) + (BigInt(0xfffff000L) -> getImmatrikulationNumber()),
            config.initial_pc
        )
    }

    def clockStep(dut: MachineModeCoreTop, state: State, config: Config) = {
        dut.clock.step()
        if ((1L << 32) - state.getMemoryValue(config.mtime) < 1) {
            state.addMemoryUpdate(Map(config.mtime -> new ValueUpdate(state.getMemoryValue(config.mtime, true), 0)))
            state.addMemoryUpdate(Map(config.mtimeh -> new ValueUpdate(state.getMemoryValue(config.mtimeh, true), state.getMemoryValue(config.mtimeh, true) + 1)))
        } else {
            state.addMemoryUpdate(Map(config.mtime -> new ValueUpdate(state.getMemoryValue(config.mtime, true), state.getMemoryValue(config.mtime, true) + 1)))
        }
    }

    def processInterrupts(dut: MachineModeCoreTop, state: State, config: Config) = {
        if ((state.getMemoryValue(config.mtime, true) >= state.getMemoryValue(config.mtimecmp, true) && state.getMemoryValue(config.mtimeh, true) == state.getMemoryValue(config.mtimecmph, true)) || state.getMemoryValue(config.mtimeh, true) > state.getMemoryValue(config.mtimecmph, true)) {
            dut.io_interrupt.m_timer.poke(true.B)
        } else {
            dut.io_interrupt.m_timer.poke(false.B)
        }
        dut.io_interrupt.m_ext.poke(false.B)
        if (dut.io_data.data_req.peekBoolean() && dut.io_data.data_we.peekBoolean() && dut.io_data.data_addr.peekInt() == config.terminal_data && state.getMemoryValue(config.terminal_ready) == 1) {
            state.addTerminalOutput(dut.io_data.data_wdata.peekInt().toChar)
            state.terminal_ready_timer = config.terminal_delay + 1
            state.addMemoryUpdate(Map(config.terminal_ready -> new ValueUpdate(state.getMemoryValue(config.terminal_ready), 0)))
        } else if (frontend.getTerminalReady().nonEmpty) {
            if (state.getMemoryValue(config.terminal_ready) == 0 && frontend.getTerminalReady().get) {
                dut.io_interrupt.m_ext.poke(true.B)
                state.addMemoryUpdate(Map(config.terminal_ready -> new ValueUpdate(state.getMemoryValue(config.terminal_ready), 1)))
            }
        } else if (state.terminal_ready_timer > 1) {
            state.terminal_ready_timer -= 1
        } else if (state.terminal_ready_timer == 1) {
            state.addMemoryUpdate(Map(config.terminal_ready -> new ValueUpdate(state.getMemoryValue(config.terminal_ready), 1)))
            dut.io_interrupt.m_ext.poke(true.B)
            state.terminal_ready_timer = 0
        }
        if (state.next_key._1) {
            dut.io_interrupt.m_ext.poke(true.B)
            state.next_key = (false, state.next_key._2, state.next_key._3)
        }
    }
    
    def assignInputs(dut: MachineModeCoreTop, state: State, config: Config) = {
        val include_pending = dut.io_data.data_addr.peekInt() match {
            case config.keyboard_data | config.keyboard_ready => true
            case _ => false
        }
        //println(dut.io_instr.instr_req.peek())
        if (dut.io_instr.instr_req.peekBoolean()) {
            dut.io_instr.instr_rdata.poke(state.getInstruction(dut.io_instr.instr_addr.peekInt()))
            dut.io_instr.instr_gnt.poke(true.B)
        } else {
            dut.io_instr.instr_gnt.poke(false.B)
        }
        if (dut.io_data.data_req.peekBoolean() && !dut.io_data.data_we.peekBoolean()) {
            val mask = dut.io_data.data_be.peekInt()
            var data = 0L
            for (i <- 0 until 4) {
                if ((mask & (1 << i)) != 0) {
                    val address = (dut.io_data.data_addr.peekInt() + i) & 0xFFFFFFFC
                    val offset = ((dut.io_data.data_addr.peekInt() + i) & 0x00000003).toInt
                    data |= ((state.getMemoryValue(address).toLong >> (offset * 8)) & 0xFF) << (i * 8)
                }
            }
            dut.io_data.data_rdata.poke(data.U)
            dut.io_data.data_gnt.poke(true.B)
            if (dut.io_data.data_addr.peekInt() == config.keyboard_data) {
                state.addMemoryUpdate(Map(config.keyboard_ready -> new ValueUpdate(state.getMemoryValue(config.keyboard_ready), 0)))
            }
        } else {
            dut.io_data.data_gnt.poke(false.B)
        }
    }

    def computeMemoryUpdate(dut: MachineModeCoreTop, state: State, config: Config) = {
        if (dut.io_data.data_req.peekBoolean() && dut.io_data.data_we.peekBoolean()) {
            println(s"Memory write at address ${dut.io_data.data_addr.peekInt()} with value ${dut.io_data.data_wdata.peekInt()} and mask ${dut.io_data.data_be.peekInt()}")
            val address = dut.io_data.data_addr.peekInt()
            val value = dut.io_data.data_wdata.peekInt().toLong
            val mask = dut.io_data.data_be.peekInt()
            dut.io_data.data_gnt.poke(true.B)
            val offset = (address % 4).toInt
            val address_1 = address & 0xFFFFFFFC
            val address_2 = address_1 + 4
            offset match {
                case 0 => {
                    var data = 0L
                    for (i <- 0 until 4) {
                        if ((mask & (1 << i)) != 0) {
                            data |= ((value >> (i * 8)) & 0xFF) << (i * 8)
                        } else {
                            data |= state.getMemoryValue(address).toLong  & (0xFF << (i * 8))
                        }
                    }
                    state.addMemoryUpdate(Map(address -> new ValueUpdate(state.getMemoryValue(address), data)))
                }
                case 1 => {
                    var data_1 = (state.getMemoryValue(address_1) & 0x000000FF).toLong
                    for (i <- 0 until 3) {
                        if ((mask & (1 << i)) != 0) {
                            data_1 |= ((value >> (i * 8)) & 0xFF) << ((i + 1) * 8)
                        } else {
                            data_1 |= state.getMemoryValue(address_1).toLong & (0xFF << ((i + 1) * 8))
                        }
                    }
                    var data_2 = (state.getMemoryValue(address_2) & 0xFFFFFF00).toLong
                    if ((mask & (1 << 3)) != 0) {
                        data_2 |= ((value >> 24) & 0xFF)
                    } else {
                        data_2 |= state.getMemoryValue(address_2).toLong & 0xFF
                    }
                    state.addMemoryUpdate(Map(address_1 -> new ValueUpdate(state.getMemoryValue(address_1), data_1)))
                    state.addMemoryUpdate(Map(address_2 -> new ValueUpdate(state.getMemoryValue(address_2), data_2)))
                }
                case 2 => {
                    var data_1 = (state.getMemoryValue(address_1) & 0x0000FFFF).toLong
                    for (i <- 0 until 2) {
                        if ((mask & (1 << i)) != 0) {
                            data_1 |= ((value >> (i * 8)) & 0xFF) << ((i + 2) * 8)
                        } else {
                            data_1 |= state.getMemoryValue(address_1).toLong & (0xFF << ((i + 2) * 8))
                        }
                    }
                    var data_2 = (state.getMemoryValue(address_2) & 0xFFFF0000).toLong
                    for (i <- 2 until 4) {
                        if ((mask & (1 << i)) != 0) {
                            data_2 |= ((value >> (i * 8)) & 0xFF) << ((i - 2) * 8)
                        } else {
                            data_2 |= state.getMemoryValue(address_2).toLong & (0xFF << ((i - 2) * 8))
                        }
                    }
                    state.addMemoryUpdate(Map(address_1 -> new ValueUpdate(state.getMemoryValue(address_1), data_1)))
                    state.addMemoryUpdate(Map(address_2 -> new ValueUpdate(state.getMemoryValue(address_2), data_2)))
                }
                case 3 => {
                    var data_1 = (state.getMemoryValue(address_1) & 0x00FFFFFF).toLong
                    for (i <- 0 until 1) {
                        if ((mask & (1 << i)) != 0) {
                            data_1 |= ((value >> (i * 8)) & 0xFF) << ((i + 3) * 8)
                        } else {
                            data_1 |= state.getMemoryValue(address_1).toLong & (0xFF << ((i + 3) * 8))
                        }
                    }
                    var data_2 = (state.getMemoryValue(address_2) & 0xFF000000).toLong
                    for (i <- 1 until 4) {
                        if ((mask & (1 << i)) != 0) {
                            data_2 |= ((value >> (i * 8)) & 0xFF) << ((i - 1) * 8)
                        } else {
                            data_2 |= state.getMemoryValue(address_2).toLong & (0xFF << ((i - 1) * 8))
                        }
                    }
                    state.addMemoryUpdate(Map(address_1 -> new ValueUpdate(state.getMemoryValue(address_1), data_1)))
                    state.addMemoryUpdate(Map(address_2 -> new ValueUpdate(state.getMemoryValue(address_2), data_2)))
                }
            }
            if (address >= config.display && address < config.display + 32 * 32 * 4) {
                offset match {
                    case 0 => {
                        val x = (((address - config.display) % (32 * 4)) / 4).toInt
                        val y = ((address - config.display) / (32 * 4)).toInt
                        var color = state.getDisplayValue((x,y))
                        for (i <- 0 until 4) {
                            if ((mask & (1 << i)) != 0) {
                                color = (color & ~(0xFF << (i * 8))) | (((value >> (i * 8)) & 0xFF) << (i * 8))
                            }
                        }
                        state.addDisplayOutput((x,y), color)
                    }
                    case 1 => {
                        val x = (((address - config.display) % (32 * 4)) / 4).toInt
                        val y = ((address - config.display) / (32 * 4)).toInt
                        var color1 = state.getDisplayValue((x,y))
                        for (i <- 0 until 3) {
                            if ((mask & (1 << i)) != 0) {
                                color1 = (color1 & ~(0xFF << ((i + 1) * 8))) | (((value >> (i * 8)) & 0xFF) << ((i + 1) * 8))
                            }
                        }
                        state.addDisplayOutput((x,y), color1)
                        var color2 = state.getDisplayValue((x,y + 1))
                        for (i <- 3 until 4) {
                            if ((mask & (1 << i)) != 0) {
                                color2 = (color2 & ~0xFF) | ((value >> 24) & 0xFF)
                            }
                        }
                        state.addDisplayOutput((x + 1,y), color2)
                    }
                    case 2 => {
                        val x = (((address - config.display) % (32 * 4)) / 4).toInt
                        val y = ((address - config.display) / (32 * 4)).toInt
                        var color1 = state.getDisplayValue((x,y))
                        for (i <- 0 until 2) {
                            if ((mask & (1 << i)) != 0) {
                                color1 = (color1 & ~(0xFF << ((i + 2) * 8))) | (((value >> (i * 8)) & 0xFF) << ((i + 2) * 8))
                            }
                        }
                        state.addDisplayOutput((x,y), color1)
                        var color2 = state.getDisplayValue((x,y + 1))
                        for (i <- 2 until 4) {
                            if ((mask & (1 << i)) != 0) {
                                color2 = (color2 & ~(0xFF << ((i - 2) * 8))) | (((value >> (i * 8)) & 0xFF) << ((i - 2) * 8))
                            }
                        }
                        state.addDisplayOutput((x + 1,y), color2)
                    }
                    case 3 => {
                        val x = (((address - config.display) % (32 * 4)) / 4).toInt
                        val y = ((address - config.display) / (32 * 4)).toInt
                        var color1 = state.getDisplayValue((x,y))
                        for (i <- 0 until 1) {
                            if ((mask & (1 << i)) != 0) {
                                color1 = (color1 & ~(0xFF << ((i + 3) * 8))) | (((value >> (i * 8)) & 0xFF) << ((i + 3) * 8))
                            }
                        }
                        state.addDisplayOutput((x,y), color1)
                        var color2 = state.getDisplayValue((x,y + 1))
                        for (i <- 1 until 4) {
                            if ((mask & (1 << i)) != 0) {
                                color2 = (color2 & ~(0xFF << ((i - 1) * 8))) | (((value >> (i * 8)) & 0xFF) << ((i - 1) * 8))
                            }
                        }
                        state.addDisplayOutput((x + 1,y), color2)
                    }
                }
                println(s"Display update at address $address with value $value")
            }
        }
    }

    def computeUpdate(dut: MachineModeCoreTop, state: State) = {
        val pc = ValueUpdate(dut.io_rvfi.rvfi_pc_rdata.peekInt(), dut.io_rvfi.rvfi_pc_wdata.peekInt())
        val reg_address = dut.io_rvfi.rvfi_rd_addr.peek().litValue.toInt
        val reg_value = dut.io_rvfi.rvfi_rd_wdata.peek().litValue

        state.addUpdate(new StateUpdate(state.next_index, pc, Map(reg_address -> new ValueUpdate(state.getRegisterValue(reg_address), reg_value)), computeCSRUpdate(dut, state), Map(), new ValueUpdate(state.getTerminalOutput(), state.getTerminalOutput()), Map()))
    }

    def computeCSRUpdate(dut: MachineModeCoreTop, state: State): Map[BigInt, ValueUpdate[BigInt]] = {
        return CSR_MAPPING.all.map(csr => {
            val old_value = state.getCSRValue(csr.litValue)
            val new_value = dut.csr_rvfi(csr).wdata.peekInt() & dut.csr_rvfi(csr).wmask.peekInt()
            if (old_value != new_value) {
                val csr_name = csr.litValue
                val csr_update = new ValueUpdate(old_value, new_value)
                csr_name -> Option(csr_update)
            } else {
                csr.litValue -> Option.empty
            }
        }).filter(o => !(o._2.isEmpty)).map(o => o._1 -> o._2.get).toMap
    }

    def getCSRValues(dut: MachineModeCoreTop): Map[BigInt, BigInt] = {
        return CSR_MAPPING.all.map(csr => {
            val new_value = dut.csr_rvfi(csr).wdata.peekInt() & dut.csr_rvfi(csr).wmask.peekInt()
            csr.litValue -> new_value
        }).toMap
    }

    def queryKeyboard(dut: MachineModeCoreTop, state: State, config: Config, frontend: TestFrontend) = {
        val (index, key) = frontend.getKey()
        var has_changed = false
        if (state.next_key._2 < index) {
            has_changed = true
            state.addMemoryUpdate(Map(config.keyboard_data -> new ValueUpdate(state.getMemoryValue(config.keyboard_data), key.toInt), config.keyboard_ready -> new ValueUpdate(state.getMemoryValue(config.keyboard_ready), 1)))
        }
        state.next_key = (has_changed, index, key)
    }

    def resetCore(dut: MachineModeCoreTop, config: Config, frontend: TestFrontend): State = {
        println("Resetting the core.")
        // Reset the core
        dut.io_reset.boot_addr.poke(config.initial_pc.U)
        dut.io_reset.rst_n.poke(false.B)
        dut.io_instr.instr_gnt.poke(false.B)
        dut.io_data.data_gnt.poke(false.B)
        dut.clock.step()
        dut.io_reset.rst_n.poke(true.B)
        dut.clock.step()
        val initialState = loadInitialState(dut, config)
        var state = new State(initialState, List())
        state.terminal_ready_timer = config.terminal_delay + 1
        //state.addMemoryUpdate(Map(config.keyboard_ready -> new ValueUpdate(state.getMemoryValue(config.keyboard_ready), 0), config.terminal_ready -> new ValueUpdate(state.getMemoryValue(config.terminal_ready), 1)))
        println("Starting simulation for " + getCategory() + " - (remaining: " + frontends.size + ")")
        frontend.publishInitialState(initialState)
        return state
    }

    def step(dut: MachineModeCoreTop, state: State, config: Config, frontend: TestFrontend) = {
        var was_valid = false
        while (!was_valid) {
            queryKeyboard(dut, state, config, frontend)
            assignInputs(dut, state, config)
            clockStep(dut, state, config)
            processInterrupts(dut, state, config)
            computeMemoryUpdate(dut, state, config)
            if (dut.io_rvfi.rvfi_valid.peekBoolean()) {
                computeUpdate(dut, state)
                val update = state.flushUpdate()
                if (!update.isEmpty) {
                    frontend.publishUpdate(update.get)
                    was_valid = true
                }
            }
        }
    }

    //def runSimulation(frontend: TestFrontend, mayExit: Boolean) = {
    s"${getCategory()}" should s"${getName()}" in {
            
        var exitting = false
        test (new MachineModeCoreTop()).withAnnotations(Seq(WriteVcdAnnotation,VerilatorBackendAnnotation)) {
            dut =>
                dut.clock.setTimeout(0)
                while (!exitting) {
                    var error = false
                    var config: Config = null
                    var state: State = null
                    // get configuration
                    try {
                        while (config == null) {
                            Thread.sleep(100)
                            config = frontend.getConfig()
                        }
                        state = resetCore(dut, config, frontend)
                    } catch {
                        case e: Exception => {
                            frontend.publishError(e.getMessage() + "\n Please upload a new configuration file once the error is resolved.")
                            println(e.getMessage())
                            error = true
                        }
                        if (frontend.mayExit()) {
                            if (frontends.size > 0) {
                                frontend = frontends.dequeue()
                            } else {
                                exitting = true
                            }
                        } else {
                            while (frontend.getAction().action != "INITIALIZE") {
                                Thread.sleep(100)
                            }
                        }
                    }
                    while (!error) {
                        var nop_counter = 0
                        try {
                            val action = frontend.getAction()
                            if (action.action != "NOP") {
                                //println(action)
                                nop_counter = 0
                            }
                            action.action match {
                                case "NOP" => {
                                    nop_counter += 1
                                    if (nop_counter > 100) {
                                        frontend.publishCurrentState(state)
                                        nop_counter = 0
                                    } else {
                                        Thread.sleep(100) // Sleep for 100ms
                                    }
                                }
                                case "STEP" => {
                                    step(dut, state, config, frontend)
                                }
                                case "RESTART" => {
                                    state = resetCore(dut, config, frontend)
                                }
                                case "RUN" => {
                                    var i = 0
                                    var stop = false
                                    while (i < action.cycles && !stop) {
                                        step(dut, state, config, frontend)
                                        i += 1
                                        if (action.delay > 0) {
                                            Thread.sleep(action.delay)
                                        }
                                        if ((i % 10 == 0) && frontend.getAction().action != "NOP") {
                                            stop = true
                                        }
                                    }
                                }
                                case "INITIALIZE" => {
                                    error = true
                                }
                                case "EXIT" => {
                                    error = true
                                    if (frontend.mayExit()) {
                                        if (frontends.size > 0) {
                                            frontend = frontends.dequeue()
                                        } else {
                                            exitting = true
                                        }
                                    } else {
                                        frontend.publishError("Cannot exit from this test.")
                                    }
                                }
                                case _ => {
                                    frontend.publishError("Invalid action: " + action.action)
                                    error = true
                                }
                            }
                        } catch {
                            case e: Exception => {
                                frontend.publishError(e.getMessage() + "\n Please restart the simulation once the error is resolved.")
                                println(e.getMessage())
                                error = true
                            }
                        }
                    }            
                }
            }
        }
    //}
}


class InteractiveTester() extends AnyFlatSpec with ChiselScalatestTester with Matchers with TesterBase {
}