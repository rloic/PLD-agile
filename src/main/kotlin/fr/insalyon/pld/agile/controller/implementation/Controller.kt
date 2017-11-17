package fr.insalyon.pld.agile.controller.implementation

import fr.insalyon.pld.agile.controller.api.Command
import fr.insalyon.pld.agile.controller.api.State
import fr.insalyon.pld.agile.model.Plan
import fr.insalyon.pld.agile.model.RoundRequest
import javafx.scene.layout.StackPane

class Controller(val window: Any) {

  val INIT_STATE: State<Any> = InitState()
  val LOADED_PLAN_STATE: State<Plan> = LoadedPlanState()
  val LOADED_DELIVERIES_STATE: State<RoundRequest> = LoadedDeliveriesState()
  val ERROR_STATE: State<Pair<Exception, State<Nothing>>> = ErrorState()

  private var currentState: State<Nothing> = INIT_STATE

  fun loadPlan() {
    try {
      currentState.loadPlan(this)
    } catch (e: Exception) {
      e.catchWithErrorState()
    }
  }

  fun loadRoundRequest() {
    try {
      currentState.loadRoundRequest(this)
    } catch (e: Exception) {
      e.catchWithErrorState()
    }
  }

  fun calculateRound() {
    try {
      currentState.calculateRound(this)
    } catch (e: Exception) {
      e.catchWithErrorState()
    }
  }

  fun ok() {
    currentState.ok(this)
  }

  fun undo(commands: List<Command>) {
    currentState.undo(this, commands)
  }

  fun redo(commands: List<Command>) {
    currentState.redo(this, commands)
  }

  private fun Exception.catchWithErrorState() {
    ERROR_STATE.init(Pair(this, currentState), window)
    currentState = ERROR_STATE
  }

  fun <T> changeStateAndInit(nextState: State<T>, initParam: T) {
    nextState.init(initParam, window)
    currentState = nextState
  }

  fun <T> changeState(nextState: State<T>) {
    currentState = nextState
  }

}