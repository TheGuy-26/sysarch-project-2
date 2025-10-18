package RISCV.interfaces.machine_mode

import chisel3._

import RISCV.model._
import scala.collection.immutable.SortedMap


class CSRIO extends Bundle {
    val priv_level = Input(PRIV_LEVEL())
    val r_data = Output(UInt(32.W))
    val we = Input(Bool())
    val w_data = Input(UInt(32.W))
    val next_r_value = Output(UInt(32.W)) // the value actually written, as some CSRs may not write the entire value
}

class CSR_RVFI extends Bundle {
    val rmask = Output(UInt(32.W))
    val wmask = Output(UInt(32.W))
    val rdata = Output(UInt(32.W))
    val wdata = Output(UInt(32.W))
}

class InterruptInterface extends Bundle {
    val m_ext = Input(Bool())
    val m_timer = Input(Bool())
}