/**
 *  Pomodoro
 *
 *  Copyright 2015 Jesse Zoldak
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Pomodoro Timer",
    namespace: "moonshot",
    author: "Jesse Zoldak",
    description: "Pomodoro Technique timer.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("Motion detector") {
        input "motion", "capability.motionSensor",
            title: "Where should there be motion?"
    }
    section("Pomodoro switch") {
        input "switch", "capability.switch",
            title: "Which switch should be activated?"
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
    subscribe(motion, "motion.active", motionActiveHandler)
}

def startThePomodoro() {
    switch.on()
}

def endThePomodoro() {
    switch.off()
}

def schedulePomodoros() {
    // Pomodoros start at 0 and 30 past the hour
    schedule("0 0,30 * * * ?", startThePomodoro)
    // Pomodoros end at 25 and 55 past the hour
    // which is when you take a 5 minute break
    schedule("0 25,55 * * * ?", endThePomodoro)
    state.isScheduled = true
}

def unschedulePomodoros() {
    unschedule()
    state.isScheduled = false
    endThePomodoro()
}

def motionActiveHandler(evt) {
    log.debug "I see motion"
    if (!state.isScheduled) {
        log.debug "Scheduling pomodoros"
        schedulePomodoros()
    }
    // Stop pomodoros if nobody has been in the area for 30 minutes
    runIn(30 * 60, unschedulePomodoros, [overwrite: true])
}
