package RISCV.model

import chisel3._
import chisel3.util._

import RISCV.utils.Types.ORTYPE
import scala.collection.mutable.HashSet

object RISCV_OP extends ChiselEnum {
  val LOAD = Value("b0000011".U)
  val MISC_MEM = Value("b0001111".U)
  val OP_IMM = Value("b0010011".U)
  val AUIPC = Value("b0010111".U)
  val STORE = Value("b0100011".U)
  val OP = Value("b0110011".U)
  val LUI = Value("b0110111".U)
  val BRANCH = Value("b1100011".U)
  val JALR = Value("b1100111".U)
  val JAL = Value("b1101111".U)
  val SYSTEM = Value("b1110011".U)
  val UNKNOWN = Value("b1111111".U)

  def apply(i: UInt): RISCV_OP.Type = {
    return Mux(RISCV_OP.safe(i)._2, RISCV_OP.safe(i)._1, RISCV_OP.UNKNOWN)
  }
}

object RISCV_FUNCT3 extends ChiselEnum {
  val F000 = Value("b000".U)
  val F001 = Value("b001".U)
  val F010 = Value("b010".U)
  val F011 = Value("b011".U)
  val F100 = Value("b100".U)
  val F101 = Value("b101".U)
  val F110 = Value("b110".U)
  val F111 = Value("b111".U)

  def apply(i: UInt): RISCV_FUNCT3.Type = {
    return Mux(
      RISCV_FUNCT3.safe(i)._2,
      RISCV_FUNCT3.safe(i)._1,
      RISCV_FUNCT3.F111
    )
  }
}

object RISCV_FUNCT7 extends ChiselEnum {
  val ZERO = Value("b0000000".U)
  val MULDIV = Value("b0000001".U)
  val SHFL = Value("b0000100".U)
  val MINMAX = Value("b0000101".U)
  val SHIFT = Value("b0010000".U)
  val SET = Value("b0010100".U)
  val SUB_SRA = Value("b0100000".U)
  val CLEAR = Value("b0100100".U)
  val ROTATE = Value("b0110000".U)
  val INV = Value("b0110100".U)
  val UNKNOWN = Value("b1111111".U)

  def apply(i: UInt): RISCV_FUNCT7.Type = {
    return Mux(
      RISCV_FUNCT7.safe(i)._2,
      RISCV_FUNCT7.safe(i)._1,
      RISCV_FUNCT7.UNKNOWN
    )
  }
}

object RISCV_FUNCT12 extends ChiselEnum {
  val ECALL = Value("b000000000000".U)
  val EBREAK = Value("b000000000001".U)
  val PAUSE = Value("b000000010000".U)
  val SRET = Value("b000100000010".U)
  val WFI = Value("b000100000101".U)
  val MRET = Value("b001100000010".U)
  val CLZ = Value("b011000000000".U)
  val CTZ = Value("b011000000001".U)
  val CPOP = Value("b011000000010".U)
  val BMATFLIP = Value("b011000000011".U)
  val SEXTB = Value("b011000000100".U)
  val SEXTH = Value("b011000000101".U)
  val CRC32B = Value("b011000010000".U)
  val CRC32H = Value("b011000010001".U)
  val CRC32W = Value("b011000010010".U)
  val CRC32D = Value("b011000010011".U)
  val CRC32CB = Value("b011000011000".U)
  val CRC32CH = Value("b011000011001".U)
  val CRC32CW = Value("b011000011010".U)
  val CRC32CD = Value("b011000011011".U)
  val FENCE_ISO = Value("b100000110011".U)
  val UNKNOWN = Value("b111111111111".U)

  def apply(i: UInt): RISCV_FUNCT12.Type = {
    return Mux(
      RISCV_FUNCT12.safe(i)._2,
      RISCV_FUNCT12.safe(i)._1,
      RISCV_FUNCT12.UNKNOWN
    )
  }
}

object RISCV_TYPE extends ChiselEnum {
  import scala.language.implicitConversions

  implicit private def enumTypeToBigInt[T <: EnumType](value: T): BigInt =
    value.litValue

  implicit private def bigIntToRiscVType[T <: BigInt](
      value: T
  ): RISCV_TYPE.Type = Value(value.U)

  val r_types = HashSet[RISCV_TYPE.Type]()
  private def addRType(
      t: RISCV_TYPE.Type
  ): RISCV_TYPE.Type = {
    r_types.add(t)
    t
  }
  def isAnRType(
      inst: UInt
  ): Bool = {
    val op = inst(6, 0)
    val funct3 = inst(14, 12)
    val funct7 = inst(31, 25)
    // vector from r_types
    val rtypes = VecInit(r_types.map(typ => typ.asUInt).toSeq)
    rtypes.contains((op << 15) + (funct3 << 12) + (funct7 << 5))
  }
  def isAnRType(
      inst: RISCV_TYPE.Type
  ): Bool = {
    val rtypes = VecInit(r_types.map(typ => typ.asUInt).toSeq)
    rtypes.contains(inst.asUInt)
  }

  val i_types = HashSet[RISCV_TYPE.Type]()
  val short_i_types = HashSet[RISCV_TYPE.Type]()
  private def addIType(
      t: RISCV_TYPE.Type
  ): RISCV_TYPE.Type = {
    i_types.add(t)
    t
  }
  private def addShortIType(
      t: RISCV_TYPE.Type
  ): RISCV_TYPE.Type = {
    short_i_types.add(t)
    t
  }
  private def isAnIType(
      inst: UInt
  ): Bool = {
    val op = inst(6, 0)
    val funct3 = inst(14, 12)
    val funct7 = inst(31, 25)
    val itypes = VecInit(i_types.map(typ => typ.asUInt).toSeq)
    val shortitypes = VecInit(short_i_types.map(typ => typ.asUInt).toSeq)
    itypes.contains((op << 15) + (funct3 << 12)) ||
    shortitypes.contains((op << 15) + (funct3 << 12) + (funct7 << 5))
  }
  private def isAnShortIType(
      inst: UInt
  ): Bool = {
    val op = inst(6, 0)
    val funct3 = inst(14, 12)
    val funct7 = inst(31, 25)
    val shortitypes = VecInit(short_i_types.map(typ => typ.asUInt).toSeq)
    shortitypes.contains((op << 15) + (funct3 << 12) + (funct7 << 5))
  }
  private def isAnLongIType(
      inst: UInt
  ): Bool = {
    val op = inst(6, 0)
    val funct3 = inst(14, 12)
    val itypes = VecInit(i_types.map(typ => typ.asUInt).toSeq)
    itypes.contains((op << 15) + (funct3 << 12))
  }

  val u_types = HashSet[RISCV_TYPE.Type]()
  private def addUType(
      t: RISCV_TYPE.Type
  ): RISCV_TYPE.Type = {
    u_types.add(t)
    t
  }
  private def isAnUType(
      inst: UInt
  ): Bool = {
    val op = inst(6, 0)
    val utypes = VecInit(u_types.map(typ => typ.asUInt).toSeq)
    utypes.contains((op << 15))
  }

  val funct12_types = HashSet[RISCV_TYPE.Type]()
  private def addFunct12Type(
      t: RISCV_TYPE.Type
  ): RISCV_TYPE.Type = {
    funct12_types.add(t)
    t
  }
  def isAnFunct12Type(
      inst: UInt
  ): Bool = {
    val op = inst(6, 0)
    val funct3 = inst(14, 12)
    val funct12 = inst(31, 20)
    val funct12types = VecInit(
      funct12_types.map(typ => typ.asUInt).toSeq
    )
    funct12types.contains((op << 15) + (funct3 << 12) + funct12)
  }
  def isAnFunct12Type(
      inst: RISCV_TYPE.Type
  ): Bool = {
    val funct12types = VecInit(
      funct12_types.map(typ => typ.asUInt).toSeq
    )
    funct12types.contains(inst.asUInt)
  }

  // | op (7) | funct3 (3) | funct7 (7) | 00000 |
  // or
  // | op (7) | funct3 (3) |    funct12 (12)    |
  def concat(op: RISCV_OP.Type): RISCV_TYPE.Type = op << 15

  def concat(funct3: RISCV_FUNCT3.Type, op: RISCV_OP.Type): RISCV_TYPE.Type =
    (op << 15) + (funct3 << 12)

  def concat[FUNCT7OR12: ORTYPE[RISCV_FUNCT7.Type, RISCV_FUNCT12.Type]#check](
      funct7or12: FUNCT7OR12,
      funct3: RISCV_FUNCT3.Type,
      op: RISCV_OP.Type
  ): RISCV_TYPE.Type = funct7or12 match {
    case funct7: RISCV_FUNCT7.Type =>
      (op << 15) + (funct3 << 12) + (funct7 << 5)
    case funct12: RISCV_FUNCT12.Type => (op << 15) + (funct3 << 12) + funct12
  }

  def getOP(t: RISCV_TYPE.Type): RISCV_OP.Type = {
    return RISCV_OP(t.asUInt(21, 15))
  }

  def getFunct3(t: RISCV_TYPE.Type): RISCV_FUNCT3.Type = {
    return RISCV_FUNCT3(t.asUInt(14, 12))
  }

  def getFunct7(t: RISCV_TYPE.Type): RISCV_FUNCT7.Type = {
    return RISCV_FUNCT7(t.asUInt(11, 5))
  }

  def getFunct12(t: RISCV_TYPE.Type): RISCV_FUNCT12.Type = {
    return RISCV_FUNCT12(t.asUInt(11, 0))
  }

  def instr_to_riscvtype(i: UInt): RISCV_TYPE.Type = {
    val op = i(6, 0)
    val funct12 = i(31, 20)
    val funct3 = i(14, 12)
    val funct7 = i(31, 25)
    val riscvtype = Wire(UInt(22.W))
    when(isAnUType(i)) {
      riscvtype := op << 15
    }.elsewhen(isAnFunct12Type(i)) {
      riscvtype := (op << 15) + (funct3 << 12) + funct12
    }.elsewhen(isAnLongIType(i)) {
      riscvtype := (op << 15) + (funct3 << 12) + (funct7 & 0.U(7.W) << 5)
    }.elsewhen(isAnShortIType(i)) {
      riscvtype := (op << 15) + (funct3 << 12) + (funct7 << 5)
    }.elsewhen(isAnRType(i)) {
      riscvtype := (op << 15) + (funct3 << 12) + (funct7 << 5)
    }.otherwise {
      riscvtype := UNKNOWN.asUInt
    }
    return suppressEnumCastWarning { RISCV_TYPE(riscvtype) }
  }

  val lb = addIType(concat(RISCV_FUNCT3.F000, RISCV_OP.LOAD))
  val lh = addIType(concat(RISCV_FUNCT3.F001, RISCV_OP.LOAD))
  val lw = addIType(concat(RISCV_FUNCT3.F010, RISCV_OP.LOAD))
  val lbu = addIType(concat(RISCV_FUNCT3.F100, RISCV_OP.LOAD))
  val lhu = addIType(concat(RISCV_FUNCT3.F101, RISCV_OP.LOAD))

  val fence = addFunct12Type(concat(RISCV_FUNCT3.F000, RISCV_OP.MISC_MEM))
  val pause = addFunct12Type(
    concat(RISCV_FUNCT12.PAUSE, RISCV_FUNCT3.F000, RISCV_OP.MISC_MEM)
  )
  val fence_iso =
    addFunct12Type(
      concat(RISCV_FUNCT12.FENCE_ISO, RISCV_FUNCT3.F000, RISCV_OP.MISC_MEM)
    )

  val addi = addIType(concat(RISCV_FUNCT3.F000, RISCV_OP.OP_IMM))
  val slli = addShortIType(
    concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
  )
  val shfli =
    addShortIType(
      concat(RISCV_FUNCT7.SHFL, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val sloi =
    addShortIType(
      concat(RISCV_FUNCT7.SHIFT, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val bseti =
    addShortIType(
      concat(RISCV_FUNCT7.SET, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val bclri =
    addShortIType(
      concat(RISCV_FUNCT7.CLEAR, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val clz =
    addFunct12Type(
      concat(RISCV_FUNCT12.CLZ, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val ctz =
    addFunct12Type(
      concat(RISCV_FUNCT12.CTZ, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val cpop =
    addFunct12Type(
      concat(RISCV_FUNCT12.CPOP, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val bmatflip = concat(
    RISCV_FUNCT12.BMATFLIP,
    RISCV_FUNCT3.F001,
    RISCV_OP.OP_IMM
  ) // Bitmanip
  val sextb =
    addFunct12Type(
      concat(RISCV_FUNCT12.SEXTB, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val sexth =
    addFunct12Type(
      concat(RISCV_FUNCT12.SEXTH, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val crc32b =
    addFunct12Type(
      concat(RISCV_FUNCT12.CRC32B, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val crc32h =
    addFunct12Type(
      concat(RISCV_FUNCT12.CRC32H, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val crc32w =
    addFunct12Type(
      concat(RISCV_FUNCT12.CRC32W, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val crc32d =
    addFunct12Type(
      concat(RISCV_FUNCT12.CRC32D, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val crc32cb = addFunct12Type(
    concat(
      RISCV_FUNCT12.CRC32CB,
      RISCV_FUNCT3.F001,
      RISCV_OP.OP_IMM
    )
  ) // Bitmanip
  val crc32ch = addFunct12Type(
    concat(
      RISCV_FUNCT12.CRC32CH,
      RISCV_FUNCT3.F001,
      RISCV_OP.OP_IMM
    )
  ) // Bitmanip
  val crc32cw = addFunct12Type(
    concat(
      RISCV_FUNCT12.CRC32CW,
      RISCV_FUNCT3.F001,
      RISCV_OP.OP_IMM
    )
  ) // Bitmanip
  val crc32cd = addFunct12Type(
    concat(
      RISCV_FUNCT12.CRC32CD,
      RISCV_FUNCT3.F001,
      RISCV_OP.OP_IMM
    )
  ) // Bitmanip
  val binvi =
    addShortIType(
      concat(RISCV_FUNCT7.INV, RISCV_FUNCT3.F001, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val slti = addIType(concat(RISCV_FUNCT3.F010, RISCV_OP.OP_IMM))
  val sltiu = addIType(concat(RISCV_FUNCT3.F011, RISCV_OP.OP_IMM))
  val xori = addIType(concat(RISCV_FUNCT3.F100, RISCV_OP.OP_IMM))
  val srli = addShortIType(
    concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F101, RISCV_OP.OP_IMM)
  )
  val unshfli =
    addShortIType(
      concat(RISCV_FUNCT7.SHFL, RISCV_FUNCT3.F101, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val sroi =
    addShortIType(
      concat(RISCV_FUNCT7.SHIFT, RISCV_FUNCT3.F101, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val gorci =
    addShortIType(
      concat(RISCV_FUNCT7.SET, RISCV_FUNCT3.F101, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val srai = addShortIType(
    concat(RISCV_FUNCT7.SUB_SRA, RISCV_FUNCT3.F101, RISCV_OP.OP_IMM)
  )
  val bexti =
    addShortIType(
      concat(RISCV_FUNCT7.CLEAR, RISCV_FUNCT3.F101, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val rori =
    addShortIType(
      concat(RISCV_FUNCT7.ROTATE, RISCV_FUNCT3.F101, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val grevi =
    addShortIType(
      concat(RISCV_FUNCT7.INV, RISCV_FUNCT3.F101, RISCV_OP.OP_IMM)
    ) // Bitmanip
  val ori = addIType(concat(RISCV_FUNCT3.F110, RISCV_OP.OP_IMM))
  val andi = addIType(concat(RISCV_FUNCT3.F111, RISCV_OP.OP_IMM))

  val auipc = addUType(concat(RISCV_OP.AUIPC))

  val sb = addIType(concat(RISCV_FUNCT3.F000, RISCV_OP.STORE))
  val sh = addIType(concat(RISCV_FUNCT3.F001, RISCV_OP.STORE))
  val sw = addIType(concat(RISCV_FUNCT3.F010, RISCV_OP.STORE))

  val add = addRType(concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F000, RISCV_OP.OP))
  val mul = addRType(
    concat(RISCV_FUNCT7.MULDIV, RISCV_FUNCT3.F000, RISCV_OP.OP)
  )
  val sub = addRType(
    concat(RISCV_FUNCT7.SUB_SRA, RISCV_FUNCT3.F000, RISCV_OP.OP)
  )
  val sll = addRType(concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F001, RISCV_OP.OP))
  val mulh = addRType(
    concat(RISCV_FUNCT7.MULDIV, RISCV_FUNCT3.F001, RISCV_OP.OP)
  )
  val shfl = addRType(concat(RISCV_FUNCT7.SHFL, RISCV_FUNCT3.F001, RISCV_OP.OP))
  val slo =
    addRType(
      concat(RISCV_FUNCT7.SHIFT, RISCV_FUNCT3.F001, RISCV_OP.OP)
    ) // Bitmanip
  val bset =
    addRType(
      concat(RISCV_FUNCT7.SET, RISCV_FUNCT3.F001, RISCV_OP.OP)
    ) // Bitmanip
  val bclr =
    addRType(
      concat(RISCV_FUNCT7.CLEAR, RISCV_FUNCT3.F001, RISCV_OP.OP)
    ) // Bitmanip
  val rol =
    addRType(
      concat(RISCV_FUNCT7.ROTATE, RISCV_FUNCT3.F001, RISCV_OP.OP)
    ) // Bitmanip
  val binv =
    addRType(
      concat(RISCV_FUNCT7.INV, RISCV_FUNCT3.F001, RISCV_OP.OP)
    ) // Bitmanip
  val slt = addRType(concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F010, RISCV_OP.OP))
  val mulhsu = addRType(
    concat(RISCV_FUNCT7.MULDIV, RISCV_FUNCT3.F010, RISCV_OP.OP)
  )
  val sh1add =
    addRType(
      concat(RISCV_FUNCT7.SHIFT, RISCV_FUNCT3.F010, RISCV_OP.OP)
    ) // Bitmanip
  val sltu = addRType(concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F011, RISCV_OP.OP))
  val mulhu = addRType(
    concat(RISCV_FUNCT7.MULDIV, RISCV_FUNCT3.F011, RISCV_OP.OP)
  )
  val xor = addRType(concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F100, RISCV_OP.OP))
  val div = addRType(
    concat(RISCV_FUNCT7.MULDIV, RISCV_FUNCT3.F100, RISCV_OP.OP)
  )
  val min = addRType(
    concat(RISCV_FUNCT7.MINMAX, RISCV_FUNCT3.F100, RISCV_OP.OP)
  )
  val sh2add =
    addRType(
      concat(RISCV_FUNCT7.SHIFT, RISCV_FUNCT3.F100, RISCV_OP.OP)
    ) // Bitmanip
  val xnor =
    addRType(
      concat(RISCV_FUNCT7.SUB_SRA, RISCV_FUNCT3.F100, RISCV_OP.OP)
    ) // Bitmanip
  val srl = addRType(concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F101, RISCV_OP.OP))
  val divu = addRType(
    concat(RISCV_FUNCT7.MULDIV, RISCV_FUNCT3.F101, RISCV_OP.OP)
  )
  val unshfl = addRType(
    concat(RISCV_FUNCT7.SHFL, RISCV_FUNCT3.F101, RISCV_OP.OP)
  )
  val minu = addRType(
    concat(RISCV_FUNCT7.MINMAX, RISCV_FUNCT3.F101, RISCV_OP.OP)
  )
  val sro =
    addRType(
      concat(RISCV_FUNCT7.SHIFT, RISCV_FUNCT3.F101, RISCV_OP.OP)
    ) // Bitmanip
  val gorc =
    addRType(
      concat(RISCV_FUNCT7.SET, RISCV_FUNCT3.F101, RISCV_OP.OP)
    ) // Bitmanip
  val sra = addRType(
    concat(RISCV_FUNCT7.SUB_SRA, RISCV_FUNCT3.F101, RISCV_OP.OP)
  )
  val bext =
    addRType(
      concat(RISCV_FUNCT7.CLEAR, RISCV_FUNCT3.F101, RISCV_OP.OP)
    ) // Bitmanip
  val ror =
    addRType(
      concat(RISCV_FUNCT7.ROTATE, RISCV_FUNCT3.F101, RISCV_OP.OP)
    ) // Bitmanip
  val grev =
    addRType(
      concat(RISCV_FUNCT7.INV, RISCV_FUNCT3.F101, RISCV_OP.OP)
    ) // Bitmanip
  val or = addRType(concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F110, RISCV_OP.OP))
  val rem = addRType(
    concat(RISCV_FUNCT7.MULDIV, RISCV_FUNCT3.F110, RISCV_OP.OP)
  )
  val max = addRType(
    concat(RISCV_FUNCT7.MINMAX, RISCV_FUNCT3.F110, RISCV_OP.OP)
  )
  val sh3add =
    addRType(
      concat(RISCV_FUNCT7.SHIFT, RISCV_FUNCT3.F110, RISCV_OP.OP)
    ) // Bitmanip
  val orn =
    addRType(
      concat(RISCV_FUNCT7.SUB_SRA, RISCV_FUNCT3.F110, RISCV_OP.OP)
    ) // Bitmanip
  val and = addRType(concat(RISCV_FUNCT7.ZERO, RISCV_FUNCT3.F111, RISCV_OP.OP))
  val remu = addRType(
    concat(RISCV_FUNCT7.MULDIV, RISCV_FUNCT3.F111, RISCV_OP.OP)
  )
  val maxu = addRType(
    concat(RISCV_FUNCT7.MINMAX, RISCV_FUNCT3.F111, RISCV_OP.OP)
  )
  val andn =
    addRType(
      concat(RISCV_FUNCT7.SUB_SRA, RISCV_FUNCT3.F111, RISCV_OP.OP)
    ) // Bitmanip

  val lui = addUType(concat(RISCV_OP.LUI))

  val beq = addIType(concat(RISCV_FUNCT3.F000, RISCV_OP.BRANCH))
  val bne = addIType(concat(RISCV_FUNCT3.F001, RISCV_OP.BRANCH))
  val blt = addIType(concat(RISCV_FUNCT3.F100, RISCV_OP.BRANCH))
  val bge = addIType(concat(RISCV_FUNCT3.F101, RISCV_OP.BRANCH))
  val bltu = addIType(concat(RISCV_FUNCT3.F110, RISCV_OP.BRANCH))
  val bgeu = addIType(concat(RISCV_FUNCT3.F111, RISCV_OP.BRANCH))

  val jalr = addUType(concat(RISCV_OP.JALR))
  val jal = addUType(concat(RISCV_OP.JAL))

  val ecall = addFunct12Type(
    concat(RISCV_FUNCT12.ECALL, RISCV_FUNCT3.F000, RISCV_OP.SYSTEM)
  )
  val ebreak = addFunct12Type(
    concat(RISCV_FUNCT12.EBREAK, RISCV_FUNCT3.F000, RISCV_OP.SYSTEM)
  )

  val sret = addFunct12Type(
    concat(RISCV_FUNCT12.SRET, RISCV_FUNCT3.F000, RISCV_OP.SYSTEM)
  )
  val wfi = addFunct12Type(
    concat(RISCV_FUNCT12.WFI, RISCV_FUNCT3.F000, RISCV_OP.SYSTEM)
  )
  val mret = addFunct12Type(
    concat(RISCV_FUNCT12.MRET, RISCV_FUNCT3.F000, RISCV_OP.SYSTEM)
  )

  val csrrw = addIType(concat(RISCV_FUNCT3.F001, RISCV_OP.SYSTEM))
  val csrrs = addIType(concat(RISCV_FUNCT3.F010, RISCV_OP.SYSTEM))
  val csrrc = addIType(concat(RISCV_FUNCT3.F011, RISCV_OP.SYSTEM))
  val csrrwi = addIType(concat(RISCV_FUNCT3.F101, RISCV_OP.SYSTEM))
  val csrrsi = addIType(concat(RISCV_FUNCT3.F110, RISCV_OP.SYSTEM))
  val csrrci = addIType(concat(RISCV_FUNCT3.F111, RISCV_OP.SYSTEM))

  val UNKNOWN =
    concat(RISCV_FUNCT12.UNKNOWN, RISCV_FUNCT3.F111, RISCV_OP.UNKNOWN)
}

object InstructionSets {
  val RV32I = Seq(
    RISCV_TYPE.lb,
    RISCV_TYPE.lh,
    RISCV_TYPE.lw,
    RISCV_TYPE.lbu,
    RISCV_TYPE.lhu,
    RISCV_TYPE.fence,
    RISCV_TYPE.pause,
    RISCV_TYPE.fence_iso,
    RISCV_TYPE.addi,
    RISCV_TYPE.slli,
    RISCV_TYPE.slti,
    RISCV_TYPE.sltiu,
    RISCV_TYPE.xori,
    RISCV_TYPE.srli,
    RISCV_TYPE.srai,
    RISCV_TYPE.ori,
    RISCV_TYPE.andi,
    RISCV_TYPE.auipc,
    RISCV_TYPE.sb,
    RISCV_TYPE.sh,
    RISCV_TYPE.sw,
    RISCV_TYPE.add,
    RISCV_TYPE.sub,
    RISCV_TYPE.sll,
    RISCV_TYPE.slt,
    RISCV_TYPE.sltu,
    RISCV_TYPE.xor,
    RISCV_TYPE.srl,
    RISCV_TYPE.sra,
    RISCV_TYPE.or,
    RISCV_TYPE.and,
    RISCV_TYPE.lui,
    RISCV_TYPE.beq,
    RISCV_TYPE.bne,
    RISCV_TYPE.blt,
    RISCV_TYPE.bge,
    RISCV_TYPE.bltu,
    RISCV_TYPE.bgeu,
    RISCV_TYPE.jalr,
    RISCV_TYPE.jal
  )

  val RV32M = Seq(
    RISCV_TYPE.mul,
    RISCV_TYPE.mulh,
    RISCV_TYPE.mulhsu,
    RISCV_TYPE.mulhu
  )

  val RV32Div = Seq(
    RISCV_TYPE.div,
    RISCV_TYPE.divu,
    RISCV_TYPE.rem,
    RISCV_TYPE.remu
  )

  val BasicBit = Seq(
    RISCV_TYPE.clz,
    RISCV_TYPE.ctz,
    RISCV_TYPE.cpop,
    RISCV_TYPE.min,
    RISCV_TYPE.max,
    RISCV_TYPE.minu,
    RISCV_TYPE.maxu
  )

  val BitPerm = Seq(
    RISCV_TYPE.grev,
    RISCV_TYPE.rol,
    RISCV_TYPE.ror,
    RISCV_TYPE.grevi,
    RISCV_TYPE.rori,
    RISCV_TYPE.shfl,
    RISCV_TYPE.unshfl,
    RISCV_TYPE.shfli,
    RISCV_TYPE.unshfli
  )

  val MachineMode = Seq(
    RISCV_TYPE.ecall,
    RISCV_TYPE.ebreak,
    RISCV_TYPE.sret,
    RISCV_TYPE.wfi,
    RISCV_TYPE.mret,
    RISCV_TYPE.csrrw,
    RISCV_TYPE.csrrs,
    RISCV_TYPE.csrrc,
    RISCV_TYPE.csrrwi,
    RISCV_TYPE.csrrsi,
    RISCV_TYPE.csrrci
  )
}
