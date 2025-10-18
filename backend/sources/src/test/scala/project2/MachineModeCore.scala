package project2

import chisel3._
import chisel3.util._

class MachineModeCore extends BlackBox with HasBlackBoxPath {
    val io = IO(new MachineModeCoreInterface)
    
    val path = sys.env.get("CORE_PATH").getOrElse("./MachineModeCore.sv")
    addPath(path)
}