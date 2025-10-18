package project2

import chisel3._

import scala.collection.immutable.SortedMap

import RISCV.interfaces.machine_mode._
import RISCV.interfaces.generic._

import RISCV.model._

class MachineModeCoreTop extends Module {
    val io_reset = IO(new ResetInterface)
    val io_instr = IO(new InstructionInterface)
    val io_data = IO(new DataInterface)
    val io_rvfi = IO(new RVFIInterface)
    implicit val csrMappingTypeOrdering: Ordering[CSR_MAPPING.Type] = Ordering.by(csr => csr.litValue)
    val csr_rvfi: SortedMap[CSR_MAPPING.Type, CSR_RVFI] = SortedMap(CSR_MAPPING.all.sortBy(csr => csr.litValue).map(x => x -> IO(new CSR_RVFI)): _*)
    val io_interrupt = IO(new InterruptInterface)

    val core = Module(new MachineModeCore)
    core.io.clock := clock
    core.io.reset := reset

    core.io.io_reset_rst_n := io_reset.rst_n
    core.io.io_reset_boot_addr := io_reset.boot_addr

    io_instr.instr_req := core.io.io_instr_instr_req
    io_instr.instr_addr := core.io.io_instr_instr_addr
    core.io.io_instr_instr_gnt := io_instr.instr_gnt
    core.io.io_instr_instr_rdata := io_instr.instr_rdata

    io_data.data_req := core.io.io_data_data_req
    io_data.data_addr := core.io.io_data_data_addr
    io_data.data_be := core.io.io_data_data_be
    io_data.data_we := core.io.io_data_data_we
    io_data.data_wdata := core.io.io_data_data_wdata
    core.io.io_data_data_gnt := io_data.data_gnt
    core.io.io_data_data_rdata := io_data.data_rdata

    io_rvfi.rvfi_valid := core.io.io_rvfi_rvfi_valid
    io_rvfi.rvfi_order := core.io.io_rvfi_rvfi_order
    io_rvfi.rvfi_insn := core.io.io_rvfi_rvfi_insn
    io_rvfi.rvfi_trap := core.io.io_rvfi_rvfi_trap
    io_rvfi.rvfi_halt := core.io.io_rvfi_rvfi_halt
    io_rvfi.rvfi_intr := core.io.io_rvfi_rvfi_intr
    io_rvfi.rvfi_mode := core.io.io_rvfi_rvfi_mode
    io_rvfi.rvfi_ixl := core.io.io_rvfi_rvfi_ixl
    io_rvfi.rvfi_rs1_addr := core.io.io_rvfi_rvfi_rs1_addr
    io_rvfi.rvfi_rs2_addr := core.io.io_rvfi_rvfi_rs2_addr
    io_rvfi.rvfi_rs1_rdata := core.io.io_rvfi_rvfi_rs1_rdata
    io_rvfi.rvfi_rs2_rdata := core.io.io_rvfi_rvfi_rs2_rdata
    io_rvfi.rvfi_rd_addr := core.io.io_rvfi_rvfi_rd_addr
    io_rvfi.rvfi_rd_wdata := core.io.io_rvfi_rvfi_rd_wdata
    io_rvfi.rvfi_pc_rdata := core.io.io_rvfi_rvfi_pc_rdata
    io_rvfi.rvfi_pc_wdata := core.io.io_rvfi_rvfi_pc_wdata
    io_rvfi.rvfi_mem_addr := core.io.io_rvfi_rvfi_mem_addr
    io_rvfi.rvfi_mem_rmask := core.io.io_rvfi_rvfi_mem_rmask
    io_rvfi.rvfi_mem_wmask := core.io.io_rvfi_rvfi_mem_wmask
    io_rvfi.rvfi_mem_rdata := core.io.io_rvfi_rvfi_mem_rdata
    io_rvfi.rvfi_mem_wdata := core.io.io_rvfi_rvfi_mem_wdata

    csr_rvfi(CSR_MAPPING.MSTATUS).rdata := core.io.csr_rvfi_0_2_rdata
    csr_rvfi(CSR_MAPPING.MSTATUS).rmask := core.io.csr_rvfi_0_2_rmask
    csr_rvfi(CSR_MAPPING.MSTATUS).wdata := core.io.csr_rvfi_0_2_wdata
    csr_rvfi(CSR_MAPPING.MSTATUS).wmask := core.io.csr_rvfi_0_2_wmask

    csr_rvfi(CSR_MAPPING.MISA).rdata := core.io.csr_rvfi_1_2_rdata
    csr_rvfi(CSR_MAPPING.MISA).rmask := core.io.csr_rvfi_1_2_rmask
    csr_rvfi(CSR_MAPPING.MISA).wdata := core.io.csr_rvfi_1_2_wdata
    csr_rvfi(CSR_MAPPING.MISA).wmask := core.io.csr_rvfi_1_2_wmask

    csr_rvfi(CSR_MAPPING.MEDELEG).rdata := core.io.csr_rvfi_2_2_rdata
    csr_rvfi(CSR_MAPPING.MEDELEG).rmask := core.io.csr_rvfi_2_2_rmask
    csr_rvfi(CSR_MAPPING.MEDELEG).wdata := core.io.csr_rvfi_2_2_wdata
    csr_rvfi(CSR_MAPPING.MEDELEG).wmask := core.io.csr_rvfi_2_2_wmask

    csr_rvfi(CSR_MAPPING.MIDELEG).rdata := core.io.csr_rvfi_3_2_rdata
    csr_rvfi(CSR_MAPPING.MIDELEG).rmask := core.io.csr_rvfi_3_2_rmask
    csr_rvfi(CSR_MAPPING.MIDELEG).wdata := core.io.csr_rvfi_3_2_wdata
    csr_rvfi(CSR_MAPPING.MIDELEG).wmask := core.io.csr_rvfi_3_2_wmask

    csr_rvfi(CSR_MAPPING.MIE).rdata := core.io.csr_rvfi_4_2_rdata
    csr_rvfi(CSR_MAPPING.MIE).rmask := core.io.csr_rvfi_4_2_rmask
    csr_rvfi(CSR_MAPPING.MIE).wdata := core.io.csr_rvfi_4_2_wdata
    csr_rvfi(CSR_MAPPING.MIE).wmask := core.io.csr_rvfi_4_2_wmask

    csr_rvfi(CSR_MAPPING.MTVEC).rdata := core.io.csr_rvfi_5_2_rdata
    csr_rvfi(CSR_MAPPING.MTVEC).rmask := core.io.csr_rvfi_5_2_rmask
    csr_rvfi(CSR_MAPPING.MTVEC).wdata := core.io.csr_rvfi_5_2_wdata
    csr_rvfi(CSR_MAPPING.MTVEC).wmask := core.io.csr_rvfi_5_2_wmask

    csr_rvfi(CSR_MAPPING.MCOUNTEREN).rdata := core.io.csr_rvfi_6_2_rdata
    csr_rvfi(CSR_MAPPING.MCOUNTEREN).rmask := core.io.csr_rvfi_6_2_rmask
    csr_rvfi(CSR_MAPPING.MCOUNTEREN).wdata := core.io.csr_rvfi_6_2_wdata
    csr_rvfi(CSR_MAPPING.MCOUNTEREN).wmask := core.io.csr_rvfi_6_2_wmask

    csr_rvfi(CSR_MAPPING.MSTATUSH).rdata := core.io.csr_rvfi_7_2_rdata
    csr_rvfi(CSR_MAPPING.MSTATUSH).rmask := core.io.csr_rvfi_7_2_rmask
    csr_rvfi(CSR_MAPPING.MSTATUSH).wdata := core.io.csr_rvfi_7_2_wdata
    csr_rvfi(CSR_MAPPING.MSTATUSH).wmask := core.io.csr_rvfi_7_2_wmask

    csr_rvfi(CSR_MAPPING.MEDELEGH).rdata := core.io.csr_rvfi_8_2_rdata
    csr_rvfi(CSR_MAPPING.MEDELEGH).rmask := core.io.csr_rvfi_8_2_rmask
    csr_rvfi(CSR_MAPPING.MEDELEGH).wdata := core.io.csr_rvfi_8_2_wdata
    csr_rvfi(CSR_MAPPING.MEDELEGH).wmask := core.io.csr_rvfi_8_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT3).rdata := core.io.csr_rvfi_9_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT3).rmask := core.io.csr_rvfi_9_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT3).wdata := core.io.csr_rvfi_9_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT3).wmask := core.io.csr_rvfi_9_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT4).rdata := core.io.csr_rvfi_10_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT4).rmask := core.io.csr_rvfi_10_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT4).wdata := core.io.csr_rvfi_10_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT4).wmask := core.io.csr_rvfi_10_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT5).rdata := core.io.csr_rvfi_11_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT5).rmask := core.io.csr_rvfi_11_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT5).wdata := core.io.csr_rvfi_11_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT5).wmask := core.io.csr_rvfi_11_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT6).rdata := core.io.csr_rvfi_12_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT6).rmask := core.io.csr_rvfi_12_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT6).wdata := core.io.csr_rvfi_12_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT6).wmask := core.io.csr_rvfi_12_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT7).rdata := core.io.csr_rvfi_13_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT7).rmask := core.io.csr_rvfi_13_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT7).wdata := core.io.csr_rvfi_13_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT7).wmask := core.io.csr_rvfi_13_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT8).rdata := core.io.csr_rvfi_14_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT8).rmask := core.io.csr_rvfi_14_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT8).wdata := core.io.csr_rvfi_14_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT8).wmask := core.io.csr_rvfi_14_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT9).rdata := core.io.csr_rvfi_15_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT9).rmask := core.io.csr_rvfi_15_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT9).wdata := core.io.csr_rvfi_15_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT9).wmask := core.io.csr_rvfi_15_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT10).rdata := core.io.csr_rvfi_16_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT10).rmask := core.io.csr_rvfi_16_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT10).wdata := core.io.csr_rvfi_16_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT10).wmask := core.io.csr_rvfi_16_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT11).rdata := core.io.csr_rvfi_17_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT11).rmask := core.io.csr_rvfi_17_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT11).wdata := core.io.csr_rvfi_17_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT11).wmask := core.io.csr_rvfi_17_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT12).rdata := core.io.csr_rvfi_18_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT12).rmask := core.io.csr_rvfi_18_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT12).wdata := core.io.csr_rvfi_18_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT12).wmask := core.io.csr_rvfi_18_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT13).rdata := core.io.csr_rvfi_19_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT13).rmask := core.io.csr_rvfi_19_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT13).wdata := core.io.csr_rvfi_19_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT13).wmask := core.io.csr_rvfi_19_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT14).rdata := core.io.csr_rvfi_20_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT14).rmask := core.io.csr_rvfi_20_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT14).wdata := core.io.csr_rvfi_20_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT14).wmask := core.io.csr_rvfi_20_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT15).rdata := core.io.csr_rvfi_21_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT15).rmask := core.io.csr_rvfi_21_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT15).wdata := core.io.csr_rvfi_21_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT15).wmask := core.io.csr_rvfi_21_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT16).rdata := core.io.csr_rvfi_22_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT16).rmask := core.io.csr_rvfi_22_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT16).wdata := core.io.csr_rvfi_22_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT16).wmask := core.io.csr_rvfi_22_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT17).rdata := core.io.csr_rvfi_23_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT17).rmask := core.io.csr_rvfi_23_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT17).wdata := core.io.csr_rvfi_23_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT17).wmask := core.io.csr_rvfi_23_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT18).rdata := core.io.csr_rvfi_24_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT18).rmask := core.io.csr_rvfi_24_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT18).wdata := core.io.csr_rvfi_24_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT18).wmask := core.io.csr_rvfi_24_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT19).rdata := core.io.csr_rvfi_25_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT19).rmask := core.io.csr_rvfi_25_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT19).wdata := core.io.csr_rvfi_25_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT19).wmask := core.io.csr_rvfi_25_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT20).rdata := core.io.csr_rvfi_26_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT20).rmask := core.io.csr_rvfi_26_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT20).wdata := core.io.csr_rvfi_26_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT20).wmask := core.io.csr_rvfi_26_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT21).rdata := core.io.csr_rvfi_27_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT21).rmask := core.io.csr_rvfi_27_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT21).wdata := core.io.csr_rvfi_27_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT21).wmask := core.io.csr_rvfi_27_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT22).rdata := core.io.csr_rvfi_28_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT22).rmask := core.io.csr_rvfi_28_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT22).wdata := core.io.csr_rvfi_28_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT22).wmask := core.io.csr_rvfi_28_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT23).rdata := core.io.csr_rvfi_29_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT23).rmask := core.io.csr_rvfi_29_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT23).wdata := core.io.csr_rvfi_29_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT23).wmask := core.io.csr_rvfi_29_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT24).rdata := core.io.csr_rvfi_30_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT24).rmask := core.io.csr_rvfi_30_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT24).wdata := core.io.csr_rvfi_30_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT24).wmask := core.io.csr_rvfi_30_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT25).rdata := core.io.csr_rvfi_31_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT25).rmask := core.io.csr_rvfi_31_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT25).wdata := core.io.csr_rvfi_31_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT25).wmask := core.io.csr_rvfi_31_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT26).rdata := core.io.csr_rvfi_32_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT26).rmask := core.io.csr_rvfi_32_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT26).wdata := core.io.csr_rvfi_32_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT26).wmask := core.io.csr_rvfi_32_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT27).rdata := core.io.csr_rvfi_33_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT27).rmask := core.io.csr_rvfi_33_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT27).wdata := core.io.csr_rvfi_33_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT27).wmask := core.io.csr_rvfi_33_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT28).rdata := core.io.csr_rvfi_34_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT28).rmask := core.io.csr_rvfi_34_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT28).wdata := core.io.csr_rvfi_34_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT28).wmask := core.io.csr_rvfi_34_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT29).rdata := core.io.csr_rvfi_35_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT29).rmask := core.io.csr_rvfi_35_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT29).wdata := core.io.csr_rvfi_35_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT29).wmask := core.io.csr_rvfi_35_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT30).rdata := core.io.csr_rvfi_36_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT30).rmask := core.io.csr_rvfi_36_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT30).wdata := core.io.csr_rvfi_36_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT30).wmask := core.io.csr_rvfi_36_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT31).rdata := core.io.csr_rvfi_37_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT31).rmask := core.io.csr_rvfi_37_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT31).wdata := core.io.csr_rvfi_37_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT31).wmask := core.io.csr_rvfi_37_2_wmask

    csr_rvfi(CSR_MAPPING.MCOUNTINHIBIT).rdata := core.io.csr_rvfi_38_2_rdata
    csr_rvfi(CSR_MAPPING.MCOUNTINHIBIT).rmask := core.io.csr_rvfi_38_2_rmask
    csr_rvfi(CSR_MAPPING.MCOUNTINHIBIT).wdata := core.io.csr_rvfi_38_2_wdata
    csr_rvfi(CSR_MAPPING.MCOUNTINHIBIT).wmask := core.io.csr_rvfi_38_2_wmask

    csr_rvfi(CSR_MAPPING.MSCRATCH).rdata := core.io.csr_rvfi_39_2_rdata
    csr_rvfi(CSR_MAPPING.MSCRATCH).rmask := core.io.csr_rvfi_39_2_rmask
    csr_rvfi(CSR_MAPPING.MSCRATCH).wdata := core.io.csr_rvfi_39_2_wdata
    csr_rvfi(CSR_MAPPING.MSCRATCH).wmask := core.io.csr_rvfi_39_2_wmask

    csr_rvfi(CSR_MAPPING.MEPC).rdata := core.io.csr_rvfi_40_2_rdata
    csr_rvfi(CSR_MAPPING.MEPC).rmask := core.io.csr_rvfi_40_2_rmask
    csr_rvfi(CSR_MAPPING.MEPC).wdata := core.io.csr_rvfi_40_2_wdata
    csr_rvfi(CSR_MAPPING.MEPC).wmask := core.io.csr_rvfi_40_2_wmask

    csr_rvfi(CSR_MAPPING.MCAUSE).rdata := core.io.csr_rvfi_41_2_rdata
    csr_rvfi(CSR_MAPPING.MCAUSE).rmask := core.io.csr_rvfi_41_2_rmask
    csr_rvfi(CSR_MAPPING.MCAUSE).wdata := core.io.csr_rvfi_41_2_wdata
    csr_rvfi(CSR_MAPPING.MCAUSE).wmask := core.io.csr_rvfi_41_2_wmask

    csr_rvfi(CSR_MAPPING.MTVAL).rdata := core.io.csr_rvfi_42_2_rdata
    csr_rvfi(CSR_MAPPING.MTVAL).rmask := core.io.csr_rvfi_42_2_rmask
    csr_rvfi(CSR_MAPPING.MTVAL).wdata := core.io.csr_rvfi_42_2_wdata
    csr_rvfi(CSR_MAPPING.MTVAL).wmask := core.io.csr_rvfi_42_2_wmask

    csr_rvfi(CSR_MAPPING.MIP).rdata := core.io.csr_rvfi_43_2_rdata
    csr_rvfi(CSR_MAPPING.MIP).rmask := core.io.csr_rvfi_43_2_rmask
    csr_rvfi(CSR_MAPPING.MIP).wdata := core.io.csr_rvfi_43_2_wdata
    csr_rvfi(CSR_MAPPING.MIP).wmask := core.io.csr_rvfi_43_2_wmask

    csr_rvfi(CSR_MAPPING.MTINST).rdata := core.io.csr_rvfi_44_2_rdata
    csr_rvfi(CSR_MAPPING.MTINST).rmask := core.io.csr_rvfi_44_2_rmask
    csr_rvfi(CSR_MAPPING.MTINST).wdata := core.io.csr_rvfi_44_2_wdata
    csr_rvfi(CSR_MAPPING.MTINST).wmask := core.io.csr_rvfi_44_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT3H).rdata := core.io.csr_rvfi_45_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT3H).rmask := core.io.csr_rvfi_45_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT3H).wdata := core.io.csr_rvfi_45_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT3H).wmask := core.io.csr_rvfi_45_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT4H).rdata := core.io.csr_rvfi_46_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT4H).rmask := core.io.csr_rvfi_46_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT4H).wdata := core.io.csr_rvfi_46_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT4H).wmask := core.io.csr_rvfi_46_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT5H).rdata := core.io.csr_rvfi_47_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT5H).rmask := core.io.csr_rvfi_47_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT5H).wdata := core.io.csr_rvfi_47_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT5H).wmask := core.io.csr_rvfi_47_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT6H).rdata := core.io.csr_rvfi_48_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT6H).rmask := core.io.csr_rvfi_48_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT6H).wdata := core.io.csr_rvfi_48_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT6H).wmask := core.io.csr_rvfi_48_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT7H).rdata := core.io.csr_rvfi_49_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT7H).rmask := core.io.csr_rvfi_49_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT7H).wdata := core.io.csr_rvfi_49_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT7H).wmask := core.io.csr_rvfi_49_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT8H).rdata := core.io.csr_rvfi_50_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT8H).rmask := core.io.csr_rvfi_50_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT8H).wdata := core.io.csr_rvfi_50_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT8H).wmask := core.io.csr_rvfi_50_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT9H).rdata := core.io.csr_rvfi_51_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT9H).rmask := core.io.csr_rvfi_51_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT9H).wdata := core.io.csr_rvfi_51_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT9H).wmask := core.io.csr_rvfi_51_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT10H).rdata := core.io.csr_rvfi_52_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT10H).rmask := core.io.csr_rvfi_52_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT10H).wdata := core.io.csr_rvfi_52_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT10H).wmask := core.io.csr_rvfi_52_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT11H).rdata := core.io.csr_rvfi_53_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT11H).rmask := core.io.csr_rvfi_53_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT11H).wdata := core.io.csr_rvfi_53_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT11H).wmask := core.io.csr_rvfi_53_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT12H).rdata := core.io.csr_rvfi_54_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT12H).rmask := core.io.csr_rvfi_54_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT12H).wdata := core.io.csr_rvfi_54_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT12H).wmask := core.io.csr_rvfi_54_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT13H).rdata := core.io.csr_rvfi_55_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT13H).rmask := core.io.csr_rvfi_55_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT13H).wdata := core.io.csr_rvfi_55_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT13H).wmask := core.io.csr_rvfi_55_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT14H).rdata := core.io.csr_rvfi_56_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT14H).rmask := core.io.csr_rvfi_56_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT14H).wdata := core.io.csr_rvfi_56_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT14H).wmask := core.io.csr_rvfi_56_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT15H).rdata := core.io.csr_rvfi_57_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT15H).rmask := core.io.csr_rvfi_57_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT15H).wdata := core.io.csr_rvfi_57_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT15H).wmask := core.io.csr_rvfi_57_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT16H).rdata := core.io.csr_rvfi_58_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT16H).rmask := core.io.csr_rvfi_58_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT16H).wdata := core.io.csr_rvfi_58_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT16H).wmask := core.io.csr_rvfi_58_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT17H).rdata := core.io.csr_rvfi_59_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT17H).rmask := core.io.csr_rvfi_59_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT17H).wdata := core.io.csr_rvfi_59_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT17H).wmask := core.io.csr_rvfi_59_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT18H).rdata := core.io.csr_rvfi_60_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT18H).rmask := core.io.csr_rvfi_60_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT18H).wdata := core.io.csr_rvfi_60_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT18H).wmask := core.io.csr_rvfi_60_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT19H).rdata := core.io.csr_rvfi_61_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT19H).rmask := core.io.csr_rvfi_61_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT19H).wdata := core.io.csr_rvfi_61_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT19H).wmask := core.io.csr_rvfi_61_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT20H).rdata := core.io.csr_rvfi_62_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT20H).rmask := core.io.csr_rvfi_62_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT20H).wdata := core.io.csr_rvfi_62_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT20H).wmask := core.io.csr_rvfi_62_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT21H).rdata := core.io.csr_rvfi_63_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT21H).rmask := core.io.csr_rvfi_63_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT21H).wdata := core.io.csr_rvfi_63_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT21H).wmask := core.io.csr_rvfi_63_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT22H).rdata := core.io.csr_rvfi_64_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT22H).rmask := core.io.csr_rvfi_64_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT22H).wdata := core.io.csr_rvfi_64_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT22H).wmask := core.io.csr_rvfi_64_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT23H).rdata := core.io.csr_rvfi_65_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT23H).rmask := core.io.csr_rvfi_65_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT23H).wdata := core.io.csr_rvfi_65_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT23H).wmask := core.io.csr_rvfi_65_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT24H).rdata := core.io.csr_rvfi_66_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT24H).rmask := core.io.csr_rvfi_66_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT24H).wdata := core.io.csr_rvfi_66_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT24H).wmask := core.io.csr_rvfi_66_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT25H).rdata := core.io.csr_rvfi_67_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT25H).rmask := core.io.csr_rvfi_67_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT25H).wdata := core.io.csr_rvfi_67_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT25H).wmask := core.io.csr_rvfi_67_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT26H).rdata := core.io.csr_rvfi_68_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT26H).rmask := core.io.csr_rvfi_68_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT26H).wdata := core.io.csr_rvfi_68_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT26H).wmask := core.io.csr_rvfi_68_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT27H).rdata := core.io.csr_rvfi_69_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT27H).rmask := core.io.csr_rvfi_69_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT27H).wdata := core.io.csr_rvfi_69_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT27H).wmask := core.io.csr_rvfi_69_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT28H).rdata := core.io.csr_rvfi_70_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT28H).rmask := core.io.csr_rvfi_70_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT28H).wdata := core.io.csr_rvfi_70_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT28H).wmask := core.io.csr_rvfi_70_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT29H).rdata := core.io.csr_rvfi_71_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT29H).rmask := core.io.csr_rvfi_71_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT29H).wdata := core.io.csr_rvfi_71_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT29H).wmask := core.io.csr_rvfi_71_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT30H).rdata := core.io.csr_rvfi_72_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT30H).rmask := core.io.csr_rvfi_72_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT30H).wdata := core.io.csr_rvfi_72_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT30H).wmask := core.io.csr_rvfi_72_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMEVENT31H).rdata := core.io.csr_rvfi_73_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT31H).rmask := core.io.csr_rvfi_73_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMEVENT31H).wdata := core.io.csr_rvfi_73_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMEVENT31H).wmask := core.io.csr_rvfi_73_2_wmask

    csr_rvfi(CSR_MAPPING.MCYCLE).rdata := core.io.csr_rvfi_74_2_rdata
    csr_rvfi(CSR_MAPPING.MCYCLE).rmask := core.io.csr_rvfi_74_2_rmask
    csr_rvfi(CSR_MAPPING.MCYCLE).wdata := core.io.csr_rvfi_74_2_wdata
    csr_rvfi(CSR_MAPPING.MCYCLE).wmask := core.io.csr_rvfi_74_2_wmask

    csr_rvfi(CSR_MAPPING.MINSTRET).rdata := core.io.csr_rvfi_75_2_rdata
    csr_rvfi(CSR_MAPPING.MINSTRET).rmask := core.io.csr_rvfi_75_2_rmask
    csr_rvfi(CSR_MAPPING.MINSTRET).wdata := core.io.csr_rvfi_75_2_wdata
    csr_rvfi(CSR_MAPPING.MINSTRET).wmask := core.io.csr_rvfi_75_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER3).rdata := core.io.csr_rvfi_76_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER3).rmask := core.io.csr_rvfi_76_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER3).wdata := core.io.csr_rvfi_76_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER3).wmask := core.io.csr_rvfi_76_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER4).rdata := core.io.csr_rvfi_77_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER4).rmask := core.io.csr_rvfi_77_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER4).wdata := core.io.csr_rvfi_77_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER4).wmask := core.io.csr_rvfi_77_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER5).rdata := core.io.csr_rvfi_78_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER5).rmask := core.io.csr_rvfi_78_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER5).wdata := core.io.csr_rvfi_78_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER5).wmask := core.io.csr_rvfi_78_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER6).rdata := core.io.csr_rvfi_79_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER6).rmask := core.io.csr_rvfi_79_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER6).wdata := core.io.csr_rvfi_79_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER6).wmask := core.io.csr_rvfi_79_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER7).rdata := core.io.csr_rvfi_80_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER7).rmask := core.io.csr_rvfi_80_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER7).wdata := core.io.csr_rvfi_80_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER7).wmask := core.io.csr_rvfi_80_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER8).rdata := core.io.csr_rvfi_81_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER8).rmask := core.io.csr_rvfi_81_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER8).wdata := core.io.csr_rvfi_81_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER8).wmask := core.io.csr_rvfi_81_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER9).rdata := core.io.csr_rvfi_82_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER9).rmask := core.io.csr_rvfi_82_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER9).wdata := core.io.csr_rvfi_82_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER9).wmask := core.io.csr_rvfi_82_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER10).rdata := core.io.csr_rvfi_83_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER10).rmask := core.io.csr_rvfi_83_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER10).wdata := core.io.csr_rvfi_83_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER10).wmask := core.io.csr_rvfi_83_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER11).rdata := core.io.csr_rvfi_84_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER11).rmask := core.io.csr_rvfi_84_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER11).wdata := core.io.csr_rvfi_84_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER11).wmask := core.io.csr_rvfi_84_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER12).rdata := core.io.csr_rvfi_85_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER12).rmask := core.io.csr_rvfi_85_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER12).wdata := core.io.csr_rvfi_85_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER12).wmask := core.io.csr_rvfi_85_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER13).rdata := core.io.csr_rvfi_86_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER13).rmask := core.io.csr_rvfi_86_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER13).wdata := core.io.csr_rvfi_86_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER13).wmask := core.io.csr_rvfi_86_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER14).rdata := core.io.csr_rvfi_87_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER14).rmask := core.io.csr_rvfi_87_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER14).wdata := core.io.csr_rvfi_87_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER14).wmask := core.io.csr_rvfi_87_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER15).rdata := core.io.csr_rvfi_88_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER15).rmask := core.io.csr_rvfi_88_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER15).wdata := core.io.csr_rvfi_88_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER15).wmask := core.io.csr_rvfi_88_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER16).rdata := core.io.csr_rvfi_89_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER16).rmask := core.io.csr_rvfi_89_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER16).wdata := core.io.csr_rvfi_89_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER16).wmask := core.io.csr_rvfi_89_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER17).rdata := core.io.csr_rvfi_90_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER17).rmask := core.io.csr_rvfi_90_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER17).wdata := core.io.csr_rvfi_90_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER17).wmask := core.io.csr_rvfi_90_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER18).rdata := core.io.csr_rvfi_91_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER18).rmask := core.io.csr_rvfi_91_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER18).wdata := core.io.csr_rvfi_91_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER18).wmask := core.io.csr_rvfi_91_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER19).rdata := core.io.csr_rvfi_92_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER19).rmask := core.io.csr_rvfi_92_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER19).wdata := core.io.csr_rvfi_92_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER19).wmask := core.io.csr_rvfi_92_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER20).rdata := core.io.csr_rvfi_93_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER20).rmask := core.io.csr_rvfi_93_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER20).wdata := core.io.csr_rvfi_93_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER20).wmask := core.io.csr_rvfi_93_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER21).rdata := core.io.csr_rvfi_94_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER21).rmask := core.io.csr_rvfi_94_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER21).wdata := core.io.csr_rvfi_94_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER21).wmask := core.io.csr_rvfi_94_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER22).rdata := core.io.csr_rvfi_95_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER22).rmask := core.io.csr_rvfi_95_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER22).wdata := core.io.csr_rvfi_95_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER22).wmask := core.io.csr_rvfi_95_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER23).rdata := core.io.csr_rvfi_96_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER23).rmask := core.io.csr_rvfi_96_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER23).wdata := core.io.csr_rvfi_96_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER23).wmask := core.io.csr_rvfi_96_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER24).rdata := core.io.csr_rvfi_97_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER24).rmask := core.io.csr_rvfi_97_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER24).wdata := core.io.csr_rvfi_97_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER24).wmask := core.io.csr_rvfi_97_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER25).rdata := core.io.csr_rvfi_98_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER25).rmask := core.io.csr_rvfi_98_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER25).wdata := core.io.csr_rvfi_98_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER25).wmask := core.io.csr_rvfi_98_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER26).rdata := core.io.csr_rvfi_99_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER26).rmask := core.io.csr_rvfi_99_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER26).wdata := core.io.csr_rvfi_99_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER26).wmask := core.io.csr_rvfi_99_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER27).rdata := core.io.csr_rvfi_100_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER27).rmask := core.io.csr_rvfi_100_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER27).wdata := core.io.csr_rvfi_100_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER27).wmask := core.io.csr_rvfi_100_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER28).rdata := core.io.csr_rvfi_101_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER28).rmask := core.io.csr_rvfi_101_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER28).wdata := core.io.csr_rvfi_101_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER28).wmask := core.io.csr_rvfi_101_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER29).rdata := core.io.csr_rvfi_102_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER29).rmask := core.io.csr_rvfi_102_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER29).wdata := core.io.csr_rvfi_102_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER29).wmask := core.io.csr_rvfi_102_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER30).rdata := core.io.csr_rvfi_103_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER30).rmask := core.io.csr_rvfi_103_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER30).wdata := core.io.csr_rvfi_103_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER30).wmask := core.io.csr_rvfi_103_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER31).rdata := core.io.csr_rvfi_104_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER31).rmask := core.io.csr_rvfi_104_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER31).wdata := core.io.csr_rvfi_104_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER31).wmask := core.io.csr_rvfi_104_2_wmask

    csr_rvfi(CSR_MAPPING.MCYCLEH).rdata := core.io.csr_rvfi_105_2_rdata
    csr_rvfi(CSR_MAPPING.MCYCLEH).rmask := core.io.csr_rvfi_105_2_rmask
    csr_rvfi(CSR_MAPPING.MCYCLEH).wdata := core.io.csr_rvfi_105_2_wdata
    csr_rvfi(CSR_MAPPING.MCYCLEH).wmask := core.io.csr_rvfi_105_2_wmask

    csr_rvfi(CSR_MAPPING.MINSTRETH).rdata := core.io.csr_rvfi_106_2_rdata
    csr_rvfi(CSR_MAPPING.MINSTRETH).rmask := core.io.csr_rvfi_106_2_rmask
    csr_rvfi(CSR_MAPPING.MINSTRETH).wdata := core.io.csr_rvfi_106_2_wdata
    csr_rvfi(CSR_MAPPING.MINSTRETH).wmask := core.io.csr_rvfi_106_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER3H).rdata := core.io.csr_rvfi_107_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER3H).rmask := core.io.csr_rvfi_107_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER3H).wdata := core.io.csr_rvfi_107_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER3H).wmask := core.io.csr_rvfi_107_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER4H).rdata := core.io.csr_rvfi_108_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER4H).rmask := core.io.csr_rvfi_108_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER4H).wdata := core.io.csr_rvfi_108_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER4H).wmask := core.io.csr_rvfi_108_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER5H).rdata := core.io.csr_rvfi_109_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER5H).rmask := core.io.csr_rvfi_109_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER5H).wdata := core.io.csr_rvfi_109_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER5H).wmask := core.io.csr_rvfi_109_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER6H).rdata := core.io.csr_rvfi_110_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER6H).rmask := core.io.csr_rvfi_110_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER6H).wdata := core.io.csr_rvfi_110_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER6H).wmask := core.io.csr_rvfi_110_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER7H).rdata := core.io.csr_rvfi_111_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER7H).rmask := core.io.csr_rvfi_111_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER7H).wdata := core.io.csr_rvfi_111_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER7H).wmask := core.io.csr_rvfi_111_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER8H).rdata := core.io.csr_rvfi_112_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER8H).rmask := core.io.csr_rvfi_112_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER8H).wdata := core.io.csr_rvfi_112_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER8H).wmask := core.io.csr_rvfi_112_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER9H).rdata := core.io.csr_rvfi_113_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER9H).rmask := core.io.csr_rvfi_113_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER9H).wdata := core.io.csr_rvfi_113_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER9H).wmask := core.io.csr_rvfi_113_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER10H).rdata := core.io.csr_rvfi_114_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER10H).rmask := core.io.csr_rvfi_114_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER10H).wdata := core.io.csr_rvfi_114_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER10H).wmask := core.io.csr_rvfi_114_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER11H).rdata := core.io.csr_rvfi_115_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER11H).rmask := core.io.csr_rvfi_115_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER11H).wdata := core.io.csr_rvfi_115_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER11H).wmask := core.io.csr_rvfi_115_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER12H).rdata := core.io.csr_rvfi_116_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER12H).rmask := core.io.csr_rvfi_116_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER12H).wdata := core.io.csr_rvfi_116_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER12H).wmask := core.io.csr_rvfi_116_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER13H).rdata := core.io.csr_rvfi_117_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER13H).rmask := core.io.csr_rvfi_117_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER13H).wdata := core.io.csr_rvfi_117_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER13H).wmask := core.io.csr_rvfi_117_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER14H).rdata := core.io.csr_rvfi_118_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER14H).rmask := core.io.csr_rvfi_118_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER14H).wdata := core.io.csr_rvfi_118_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER14H).wmask := core.io.csr_rvfi_118_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER15H).rdata := core.io.csr_rvfi_119_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER15H).rmask := core.io.csr_rvfi_119_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER15H).wdata := core.io.csr_rvfi_119_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER15H).wmask := core.io.csr_rvfi_119_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER16H).rdata := core.io.csr_rvfi_120_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER16H).rmask := core.io.csr_rvfi_120_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER16H).wdata := core.io.csr_rvfi_120_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER16H).wmask := core.io.csr_rvfi_120_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER17H).rdata := core.io.csr_rvfi_121_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER17H).rmask := core.io.csr_rvfi_121_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER17H).wdata := core.io.csr_rvfi_121_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER17H).wmask := core.io.csr_rvfi_121_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER18H).rdata := core.io.csr_rvfi_122_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER18H).rmask := core.io.csr_rvfi_122_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER18H).wdata := core.io.csr_rvfi_122_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER18H).wmask := core.io.csr_rvfi_122_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER19H).rdata := core.io.csr_rvfi_123_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER19H).rmask := core.io.csr_rvfi_123_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER19H).wdata := core.io.csr_rvfi_123_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER19H).wmask := core.io.csr_rvfi_123_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER20H).rdata := core.io.csr_rvfi_124_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER20H).rmask := core.io.csr_rvfi_124_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER20H).wdata := core.io.csr_rvfi_124_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER20H).wmask := core.io.csr_rvfi_124_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER21H).rdata := core.io.csr_rvfi_125_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER21H).rmask := core.io.csr_rvfi_125_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER21H).wdata := core.io.csr_rvfi_125_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER21H).wmask := core.io.csr_rvfi_125_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER22H).rdata := core.io.csr_rvfi_126_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER22H).rmask := core.io.csr_rvfi_126_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER22H).wdata := core.io.csr_rvfi_126_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER22H).wmask := core.io.csr_rvfi_126_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER23H).rdata := core.io.csr_rvfi_127_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER23H).rmask := core.io.csr_rvfi_127_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER23H).wdata := core.io.csr_rvfi_127_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER23H).wmask := core.io.csr_rvfi_127_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER24H).rdata := core.io.csr_rvfi_128_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER24H).rmask := core.io.csr_rvfi_128_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER24H).wdata := core.io.csr_rvfi_128_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER24H).wmask := core.io.csr_rvfi_128_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER25H).rdata := core.io.csr_rvfi_129_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER25H).rmask := core.io.csr_rvfi_129_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER25H).wdata := core.io.csr_rvfi_129_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER25H).wmask := core.io.csr_rvfi_129_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER26H).rdata := core.io.csr_rvfi_130_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER26H).rmask := core.io.csr_rvfi_130_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER26H).wdata := core.io.csr_rvfi_130_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER26H).wmask := core.io.csr_rvfi_130_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER27H).rdata := core.io.csr_rvfi_131_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER27H).rmask := core.io.csr_rvfi_131_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER27H).wdata := core.io.csr_rvfi_131_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER27H).wmask := core.io.csr_rvfi_131_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER28H).rdata := core.io.csr_rvfi_132_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER28H).rmask := core.io.csr_rvfi_132_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER28H).wdata := core.io.csr_rvfi_132_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER28H).wmask := core.io.csr_rvfi_132_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER29H).rdata := core.io.csr_rvfi_133_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER29H).rmask := core.io.csr_rvfi_133_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER29H).wdata := core.io.csr_rvfi_133_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER29H).wmask := core.io.csr_rvfi_133_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER30H).rdata := core.io.csr_rvfi_134_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER30H).rmask := core.io.csr_rvfi_134_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER30H).wdata := core.io.csr_rvfi_134_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER30H).wmask := core.io.csr_rvfi_134_2_wmask

    csr_rvfi(CSR_MAPPING.MHPMCOUNTER31H).rdata := core.io.csr_rvfi_135_2_rdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER31H).rmask := core.io.csr_rvfi_135_2_rmask
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER31H).wdata := core.io.csr_rvfi_135_2_wdata
    csr_rvfi(CSR_MAPPING.MHPMCOUNTER31H).wmask := core.io.csr_rvfi_135_2_wmask

    csr_rvfi(CSR_MAPPING.MVENDORID).rdata := core.io.csr_rvfi_136_2_rdata
    csr_rvfi(CSR_MAPPING.MVENDORID).rmask := core.io.csr_rvfi_136_2_rmask
    csr_rvfi(CSR_MAPPING.MVENDORID).wdata := core.io.csr_rvfi_136_2_wdata
    csr_rvfi(CSR_MAPPING.MVENDORID).wmask := core.io.csr_rvfi_136_2_wmask

    csr_rvfi(CSR_MAPPING.MARCHID).rdata := core.io.csr_rvfi_137_2_rdata
    csr_rvfi(CSR_MAPPING.MARCHID).rmask := core.io.csr_rvfi_137_2_rmask
    csr_rvfi(CSR_MAPPING.MARCHID).wdata := core.io.csr_rvfi_137_2_wdata
    csr_rvfi(CSR_MAPPING.MARCHID).wmask := core.io.csr_rvfi_137_2_wmask

    csr_rvfi(CSR_MAPPING.MIMPID).rdata := core.io.csr_rvfi_138_2_rdata
    csr_rvfi(CSR_MAPPING.MIMPID).rmask := core.io.csr_rvfi_138_2_rmask
    csr_rvfi(CSR_MAPPING.MIMPID).wdata := core.io.csr_rvfi_138_2_wdata
    csr_rvfi(CSR_MAPPING.MIMPID).wmask := core.io.csr_rvfi_138_2_wmask

    csr_rvfi(CSR_MAPPING.MHARTID).rdata := core.io.csr_rvfi_139_2_rdata
    csr_rvfi(CSR_MAPPING.MHARTID).rmask := core.io.csr_rvfi_139_2_rmask
    csr_rvfi(CSR_MAPPING.MHARTID).wdata := core.io.csr_rvfi_139_2_wdata
    csr_rvfi(CSR_MAPPING.MHARTID).wmask := core.io.csr_rvfi_139_2_wmask

    csr_rvfi(CSR_MAPPING.MCONFIGPTR).rdata := core.io.csr_rvfi_140_2_rdata
    csr_rvfi(CSR_MAPPING.MCONFIGPTR).rmask := core.io.csr_rvfi_140_2_rmask
    csr_rvfi(CSR_MAPPING.MCONFIGPTR).wdata := core.io.csr_rvfi_140_2_wdata
    csr_rvfi(CSR_MAPPING.MCONFIGPTR).wmask := core.io.csr_rvfi_140_2_wmask

    csr_rvfi(CSR_MAPPING.UNKNOWN).rdata := core.io.csr_rvfi_141_2_rdata
    csr_rvfi(CSR_MAPPING.UNKNOWN).rmask := core.io.csr_rvfi_141_2_rmask
    csr_rvfi(CSR_MAPPING.UNKNOWN).wdata := core.io.csr_rvfi_141_2_wdata
    csr_rvfi(CSR_MAPPING.UNKNOWN).wmask := core.io.csr_rvfi_141_2_wmask

    core.io.io_interrupt_m_ext := io_interrupt.m_ext
    core.io.io_interrupt_m_timer := io_interrupt.m_timer
}