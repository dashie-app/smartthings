/**
 *  dashie-connect 
 *
 *  Copyright 2020 Tobias Haerke
 *  Based on smartthings-rest-api by Julian Werfel https://github.com/Jwerfel/smartthings-rest-api
 *
 *  Licensed under The GNU General Public License is a free, copyleft license for
software and other kinds of works. 
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "dashie-connect",
    namespace: "dashie-app.dashie-connect",
    author: "Tobias Haerke",
    description: "Connect to dashie.app",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "dashie-app", displayLink: ""]
)

mappings {
  path("/devices") {
    action: [
        GET: "listDevices"
    ]
  }
  path("/device/:id") {
    action: [
        GET: "deviceDetails"
    ]
  }
  path("/device/:id/attribute/:name") {
    action: [
        GET: "deviceGetAttributeValue"
    ]
  }
  path("/device/:id/attributes") {
    action: [
      GET: "deviceGetAttributes"
    ]
  }
  path("/devices/attribute/:name") {
    action: [
        GET: "deviceGetAttributeValueForDevices"
    ]
  }
  path("/devices/attributes") {
    action: [
      GET: "devicesGetAttributes"
    ]
  }
  path("/device/:id/command/:name") {
    action: [
        POST: "deviceCommand"
    ]
  }
  path("/device/status/:id") {
  	action: [
    	GET: "deviceStatus"
    ]
  }
  path("/devices/statuses") {
  	action: [
    	GET: "devicesStatuses"
    ]
  }
	path("/device/events/:id") {
    action: [
      GET: "deviceEvents"
    ]
  }
  path("/test") {
  	action: [
      GET: "test"
    ]
  }
  path("/routines") {
  	action: [
      GET: "getRoutines"
    ]
  }
  path("/routine") {
    action: [
      POST: "executeRoutine"
    ]
  }
  path("/modes") {
    action: [
      GET: "getModes"
    ]
  }
  path("/mode") {
    action: [
      GET: "getCurrentMode",
      POST: "setCurrentMode"
    ]
  }
}

preferences {
  section() {
    input "devices", "capability.actuator", title: "Devices", multiple: true, required: false
    input "sensors", "capability.sensor", title: "Sensors", multiple: true, required: false
    input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", multiple: true, required: false
    input "presenceSensor", "capability.presenceSensor", title: "Presence", multiple: true, required: false
  }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  initialize()
}

def initialize() {
}

def listDevices() {
  def resp = []
  devices.each {
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName
    ]
  }
  sensors.each {
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName
    ]
  }
    temperatures.each {
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName
    ]
  }
  presenceSensor.each {
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName
    ]
  }
  return resp
}

def deviceDetails() {
  def device = getDeviceById(params.id)

  def supportedAttributes = []
  device.supportedAttributes.each {
    supportedAttributes << it.name
  }

  def supportedCommands = []
  device.supportedCommands.each {
    def arguments = []
    it.arguments.each { arg ->
      arguments << "" + arg
    }
    supportedCommands << [
        name: it.name,
        arguments: arguments
    ]
  }

  return [
      id: device.id,
      label: device.label,
      manufacturerName: device.manufacturerName,
      modelName: device.modelName,
      name: device.name,
      displayName: device.displayName,
      supportedAttributes: supportedAttributes,
      supportedCommands: supportedCommands
  ]
}

def devicesGetAttributes() {
  def resp = [];
  def devicesString = params.devices;
  def attributesString = params.attributes;
  def deviceIds = devicesString.split(',');
  def attributeNames = attributesString.split(',');
  def lastEvent = false;
  def lastEventParam = params.lastEvent;
  log.info("LogEventParam: "+ logEventParam);
  if(lastEventParam == 'true')
  	lastEvent = true;

  deviceIds.each {d ->
    def device = getDeviceById(d);
    if(device != null) {
      def deviceStatus = device.getStatus();
      def lastActivity = device.getLastActivity();
      def mostRecentEvent = null;
      def mostRecentEventDate = null;
      if(lastEvent == true) {
      	def deviceEvents = device.events(max: 1);
        if(deviceEvents.size() > 0) {
        	//mostRecentEvent = deviceEvents[0].name + " - " +deviceEvents[0].stringValue;
            mostRecentEvent = deviceEvents[0].stringValue;
            mostRecentEventDate = deviceEvents[0].date;
        }
      }
      	
      attributeNames.each {a -> 
        def value = device.currentValue(a);        
        resp << [
          id: d,
          name: a,
          value: value,
          deviceStatus: deviceStatus,
          lastActivity: lastActivity,
          mostRecentEvent: mostRecentEvent,
          mostRecentEventDate: mostRecentEventDate
        ]
      }
    }
    else {
      log.warn("Could not find device " + d);
    }
  }
  return resp;
}

def deviceGetAttributeValueForDevices() {
  def resp = []

  def args = params.arg
  //log.info("Args: " + args);
    
  def deviceIds = args.split(',');
  //log.info("deviceIds: " + deviceIds);
  def name = params.name
  //log.info("ParamName: " + name);

  deviceIds.each {
    def device = getDeviceById(it);
    if(device != null) {
        def value = device.currentValue(name);
        resp << [
          id: it,
          value: value
        ]
    }
    else {
    	log.warn("Could not find device " + it);
    }

  }

  return resp;
}

def deviceGetAttributeValue() {
  def device = getDeviceById(params.id)
  def name = params.name
  def value = device.currentValue(name);
  return [
      value: value
  ]
}

def deviceGetAttributes() {
  def device = getDeviceById(params.id);
  def args = params.arg;
  def attributes = args.split(',');
  def resp = [];
  attributes.each {
    def value = device.currentValue(it);
    resp << [
      name: it,
      value: value
    ]
  }
  return resp;
}

def deviceStatus() {
	def device = getDeviceById(params.id)
    //log.warn("Getting status for device: " + device);
     def status = device.getStatus();
     //log.warn("Status for device is: " + status);
     return [
     	value: status
     ]
}

def devicesStatuses() {
  def resp = []
  def args = params.devices
  def deviceIds = args.split(',');
  deviceIds.each {
    def device = getDeviceById(it);
    if(device != null) {
        def value = device.getStatus();
        resp << [
          id: it,
          value: value
        ]
    }
    else {
    	log.warn("Could not find device " + it);
    }

  }

  return resp;
}

def deviceCommand() {
  def device = getDeviceById(params.id)
  def name = params.name
  def args = params.arg
  def isIntParam = params.isInt;
  def isInt = false;
  if("true".equalsIgnoreCase(isIntParam)) {
  	isInt = true;
  }
  if (args == null) {
    args = []
  } else if (args instanceof String) {
    args = [args]
  }
  log.debug "device command: ${name} ${args}"  
  switch(args.size) {
    case 0:
      device."$name"()
      break;
    case 1:
      //log.debug("Arg0 value: " + args[0]);
      def val = args[0];
      if(isInt) {
      	int num = args[0] as Integer
      	device."$name"(num)
      }
      else {
      	device."$name"(args[0])
      }
      //device.setCoolingSetpoint(args[0]);
      break;
    case 2:
      device."$name"(args[0], args[1])
      break;
    default:
      throw new Exception("Unhandled number of args")
  }
}

def deviceEvents() {
	def numEvents = 20
    
  def lastEvent = params.lastEvent;
  def device = getDeviceById(params.id);
  
  def events = null;
  
  if(lastEvent == null) {
  	events = device.events();
  }
  else {
  	// date: "2019-07-08T14:20:06Z"
  	//def dateFormat = new java.util.SimpleDateFormat("yyyy-mm-ddThh:mm:ssZ");
    log.debug("Parsing date: " + lastEvent);
  	//def date = Date.parse("yyyy-mm-ddThh:mm:ssZ", lastEvent);// dateFormat.parse(dateFormat);
    def date = Date.parse("yyyy-MM-dd HH:mm:ss z", lastEvent);// dateFormat.parse(dateFormat);
    log.debug("Parsed date is: "+ date);
    def endDate = new Date() - 10; // only 7 days should exist, 
    log.debug("Searching for events between [" + endDate + "] and [" + date + "]");
    events = device.eventsBetween(endDate, date);
    //events = device.eventsBetween(date, endDate, [max: 5]);
    log.debug("Found [" + events.size() +"] in range");
  }
  
  if(events.size() > 0) {
  	def last = events.size() - 1;    
  	log.debug("Event[" + last + "].date = " + events.get(last).date);
  }
  //log.debug("Got [" + events.size() + "] events");
  
  def resp = [];
  events.each {
    resp << [
      stringValue: it.stringValue,
      source: it.source,
      name: it.name,
      descriptionText: it.descriptionText,
      date: it.date,
      description: it.description,
      //jsonValue: it.jsonValue,
      value: it.value,
      linkText: it.linkText
    ]
  }

  return resp;
}

def getRoutines() {
	def actions = location.helloHome?.getPhrases()*.label;
    return actions;
}

def executeRoutine(){
	def name = params.name;
    log.info("Executing routine: " + name);	
    location.helloHome?.execute(name)
}

def getModes() {
  return location.modes
}

def getCurrentMode() {
  return getModes()?.find {it.name == location.mode}
}

def setCurrentMode() {
  def mode = request?.JSON;
    
    log.info("Executing setModes mode: " + mode);
    
    if (mode && mode.id) {
      def found = getModes()?.find {it.name == location.mode};
        if (found) {
        log.info("setModes found: " + found);
          setLocationMode(found);
        }
    }
}


def test() {

/*

	if(location != null)
    	console.log("Location not null");
    def helloHome = location.helloHome;
    if(helloHome != null)
    	console.log("Hello Home: " + helloHome);
*/
	def actions = location.helloHome?.getPhrases()*.label;
    //console.log("Actions: " + actions);
    //console.log("Got [" + actions.size() + "] actions");
    
    /*
    actions.each {
    	console.log("Action: " + it);
    }*/
    return actions;
}

def getDeviceById(id) {
  def device = devices.find { it.id == id }
  if(device == null)
  	device = sensors.find{it.id == id}
  if(device == null)
  	device = temperatures.find{it.id == id}
  if(device == null)
    device = presenceSensor.find{it.id == id}
  return device;
}
