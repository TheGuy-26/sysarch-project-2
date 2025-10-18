package controllers

import javax.inject._
import org.apache.pekko.actor._
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream._
import play.api.mvc._
import play.api.libs.json._
import org.apache.pekko.stream.scaladsl._
import org.apache.pekko.util.ByteString
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.collection.mutable.Queue
import models._
import java.awt.Desktop.Action
import scala.reflect.internal.Reporter.ERROR
import play.api.libs.json.Json
import java.io.File

@Singleton
class WebUIController @Inject() (cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) extends AbstractController(cc) {

  val DEGUG = true
  var state: State = null;
  var actionsBuffer = Queue[UIAction]()
  var key: (Int, Char) = (0, '\u0000')
  var config: Config = null

  def index = Action {
    Ok(views.html.main())
  }

// Define a WebSocket endpoint for clients to receive news updates
def socket = WebSocket.accept[String, String] { request =>
  if (DEGUG)
    println("WebSocket connected")
  val sourceQueue = Source.queue[String](bufferSize = 10)
    .mapMaterializedValue { queue =>
      updateListeners += queue
    }
  Flow.fromSinkAndSource(Sink.ignore, sourceQueue)
}

var updateListeners = ListBuffer[BoundedSourceQueue[String]]()

// Define a controller action to handle POST requests for starting a new simulation
def startSimulation = Action(parse.json) { request =>
  if (DEGUG)
    println("Received initial state: " + (request.body).as[InitialState])
  val initialState = (request.body).as[InitialState]
  state = new State(initialState, List())
  val json = Json.obj("type" -> "INITIAL-STATE", "state" -> Json.toJson(initialState))
  updateListeners.foreach(_.offer(json.toString()))
  key = (0, '\u0000')
  Ok("Simulation started")
}

def currentState = Action(parse.json) { request =>
  if (DEGUG)
    println("Received current state: " + (request.body).as[State])
  if (state == null) {
    if (DEGUG)
      println("State not yet set. Using received state.")
    state = (request.body).as[State]
    val json = Json.obj("type" -> "CURRENT-STATE", "state" -> Json.toJson(state))
    updateListeners.foreach(_.offer(json.toString()))
    Ok("State updated")
  } else {
    Ok("State already exists")
  }
}

def updateState = Action(parse.json) { request =>
  if (DEGUG)
    println("Received state update: " + (request.body).as[StateUpdate])
  val update = request.body.as[StateUpdate]
  state.addReceivedUpdate(update)
  val json = Json.obj("type" -> "STATE-UPDATE", "update" -> Json.toJson(update))
  updateListeners.foreach(_.offer(json.toString()))
  Ok("State updated")
}

def getState = Action {
  if (state == null) {
    NotFound("State not yet set. Please try again later.")
  } else {
    Ok(Json.toJson(state))
  }
}

def createAction = Action(parse.json) { request =>
  if (DEGUG)
    println("Received action: " + (request.body).as[UIAction])
  val actionType = (request.body \ "action").as[String]
  val delay = (request.body \ "delay").asOpt[String].getOrElse("0").toInt
  val cycles = (request.body \ "cycles").asOpt[String].getOrElse("1").toInt
  actionsBuffer.enqueue(new UIAction(actionType, delay, cycles))
  Ok("Action created")
}

def getAction = Action {
  if (actionsBuffer.isEmpty) {
    Ok(Json.toJson(new UIAction("NOP")))
  } else {
    val action = actionsBuffer.dequeue()
    Ok(Json.toJson(action))
  }
}

def setKey = Action(parse.json) { request =>
  if (DEGUG)
    println("Received key: " + (request.body \ "key").as[String])
  key = (key._1 + 1, (request.body \ "key").as[String].charAt(0))
  Ok("Key set")
}

def getKey = Action {
  Ok(new JsObject(Map("index" -> JsString(key._1.toString()), "key" -> JsString(key._2.toString()))))
}

def setConfig = Action(parse.json) { request =>
  try {
    if (DEGUG)
      println("Received config: " + (request.body \ "config").as[Config])
    config = (request.body \ "config").as[Config]
    actionsBuffer.enqueue(new UIAction("INITIALIZE"))
  } catch {
    case e: Exception => {
      if (DEGUG)
        println("Error setting config: " + e)
      val error = "Invalid config. Please check the syntax and try again."
      val json = Json.obj("type" -> "ERROR-MESSAGE", "message" -> error)
      updateListeners.foreach(_.offer(json.toString()))
      BadRequest("Error setting config")
    }
  }
  Ok("Config set")
}

def getConfig = Action {
  if (config == null) {
    NotFound("Config not yet set. Please try again later.")
  } else {
    Ok(Json.toJson(config))
  }
}

def setError = Action(parse.json) { request =>
  if (DEGUG)
    println("Received error:" + (request.body).as[String])
  val error = (request.body).as[String]
  val json = Json.obj("type" -> "ERROR-MESSAGE", "message" -> error)
  updateListeners.foreach(_.offer(json.toString()))
  Ok("Error sent")
}

def getAvailableConfigs = Action {
  if (DEGUG)
    println("Received request for available configs")
  val path = sys.env.get("CONFIG_PATH").getOrElse("/configs").toString

  val configDir = new File(path)
  var configs = List[Config]()
  if (configDir.exists && configDir.isDirectory) {
    val jsonFiles = configDir.listFiles.filter(f => f.isFile && f.getName.endsWith(".json"))
    jsonFiles.foreach { file =>
      val fileContents = scala.io.Source.fromFile(file).getLines().mkString
      val configOpt = try {
        Some(Json.parse(fileContents).as[Config])
      } catch {
        case _: Exception => None
      }
      configOpt match {
        case Some(config) => configs = config :: configs
        case None => if (DEGUG) println(s"Failed to parse config file: ${file.getName}")
      }
    }
  }

  if (configs.isEmpty) {
    NotFound("No available configs found.")
  } else {
    Ok(Json.toJson(configs))
  }
}

}