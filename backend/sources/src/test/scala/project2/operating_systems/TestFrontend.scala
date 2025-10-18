package project2.operating_systems

import project2.utils.models.{InitialState, Config, UIAction, StateUpdate, State}

abstract class TestFrontend {
    def getKey(): (Int, Char)
    def getAction(): UIAction
    def publishCurrentState(state: State): Unit
    def publishInitialState(initialState: InitialState): Unit
    def publishUpdate(update: StateUpdate): Unit
    def getConfig(): Config
    def publishError(error: String): Unit
    def mayExit(): Boolean
    def getCategory(): String
    def getName(): String
    def getTerminalReady(): Option[Boolean] = Option.empty
}
