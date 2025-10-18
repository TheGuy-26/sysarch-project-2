package project2.utils.models

import sttp.client4.quick._
import sttp.client4.Response
import play.api.libs.json._


case class State(
    val initialState: InitialState,
    var updates: List[StateUpdate]
) {
    var pendingUpdate = Option.empty[StateUpdate]
    var next_index = 1
    var next_key: (Boolean, Int, Char) = (false, 0, '\u0000')
    var terminal_ready_timer = BigInt(0)

    def addUpdate(update: StateUpdate) = {
        if (pendingUpdate.isEmpty) {
            pendingUpdate = Option(update)
        } else {
            pendingUpdate = Option(pendingUpdate.get.merge(update))
        }
    }

    def addMemoryUpdate(mem_update: Map[BigInt, ValueUpdate[BigInt]]) = {
        if (pendingUpdate.isEmpty) {
            pendingUpdate = Option(new StateUpdate(next_index, new ValueUpdate(getPCValue(), getPCValue()), Map(), Map(), mem_update, new ValueUpdate(getTerminalOutput(), getTerminalOutput()), Map()))
        } else {
            val update = new StateUpdate(pendingUpdate.get.index, new ValueUpdate(getPCValue(), getPCValue()), Map(), Map(), mem_update, new ValueUpdate(getTerminalOutput(), getTerminalOutput()), Map())
            pendingUpdate = Option(pendingUpdate.get.merge(update))
        }
    }

    def addTerminalOutput(output: Char) = {
        if (pendingUpdate.isEmpty) {
            pendingUpdate = Option(new StateUpdate(next_index, new ValueUpdate(getPCValue(), getPCValue()), Map(), Map(), Map(), new ValueUpdate(getTerminalOutput(), getTerminalOutput() + output), Map()))
        } else {
            val update = new StateUpdate(pendingUpdate.get.index, new ValueUpdate(getPCValue(), getPCValue()), Map(), Map(), Map(), new ValueUpdate(getTerminalOutput(), getTerminalOutput() + output), Map())
            pendingUpdate = Option(pendingUpdate.get.merge(update))
        }
    }

    def addDisplayOutput(address: (Int, Int), value: BigInt) = {
        if (pendingUpdate.isEmpty) {
            pendingUpdate = Option(new StateUpdate(next_index, new ValueUpdate(getPCValue(), getPCValue()), Map(), Map(), Map(), new ValueUpdate(getTerminalOutput(), getTerminalOutput()), Map((address, new ValueUpdate(getDisplayValue(address), value)))))
        } else {
            val update = new StateUpdate(pendingUpdate.get.index, new ValueUpdate(getPCValue(), getPCValue()), Map(), Map(), Map(), new ValueUpdate(getTerminalOutput(), getTerminalOutput()), Map((address, new ValueUpdate(getDisplayValue(address), value))))
            pendingUpdate = Option(pendingUpdate.get.merge(update))
        }
    }

    def getInstruction(address: BigInt): BigInt = {
        initialState.getInstruction(address)
    }

    def getMemoryValue(address: BigInt, includePending: Boolean = false): BigInt = {
        if (includePending && !pendingUpdate.isEmpty) {
            return pendingUpdate.get.getMemoryValue(address).getOrElse(getMemoryValue(address, false))
        }
        return updates.map(u => u.getMemoryValue(address)).findLast(u => !u.isEmpty).getOrElse(initialState.getMemoryValue(address)).getOrElse(BigInt(0))
    }

    def getRegisterValue(address: Int): BigInt = {
        updates.map(u => u.getRegisterValue(address)).findLast(u => !u.isEmpty).getOrElse(initialState.getRegisterValue(address)).getOrElse(BigInt(0))
    }

    def getCSRValue(address: BigInt): BigInt = {
        updates.map(u => u.getCSRValue(address)).findLast(u => !u.isEmpty).getOrElse(initialState.getCSRValue(address)).getOrElse(BigInt(0))
    }

    def getPCValue(): BigInt = {
        if (updates.isEmpty) {
            initialState.pc
        } else {
            updates.last.getPCValue()
        }
    }

    def getTerminalOutput(): String = {
        if (updates.isEmpty) {
            ""
        } else {
            updates.last.terminal_output.newValue
        }
    }

    def getDisplayValue(address: (Int, Int)): BigInt = {
        updates.map(u => u.getDisplayValue(address)).findLast(u => !u.isEmpty).getOrElse(Option(BigInt(0))).get
    }

    def flushUpdate(): Option[StateUpdate] = {
        if (!pendingUpdate.isEmpty) {
            val update = pendingUpdate.get
            updates = updates :+ update
            pendingUpdate = Option.empty
            next_index += 1
            return Option(update)
        }
        return Option.empty
    }
}

object State {
    implicit val reads : Reads[State] = new Reads[State] {
        def reads(json: JsValue): JsResult[State] = {
            val initialState = (json \ "initial_state").as[InitialState]
            val updates = (json \ "updates").as[List[StateUpdate]]
            JsSuccess(new State(initialState, updates))
        }
    }

    implicit val writes : Writes[State] = new Writes[State] {
        def writes(state: State): JsValue = {
            Json.obj(
                "initial_state" -> Json.toJson(state.initialState),
                "updates" -> Json.toJson(state.updates)
            )
        }
    }
}