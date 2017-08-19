#!/usr/bin/env groovy

@NonCPS
def call(String branch, String agentId, Map deploy) {
  for ( e in deploy ) {
    if (e.key == branch){
      if (agentId) {
        sshagent([agentId]) {
          mmRvm(e.value)
        }
      } else {
        mmRvm(e.value)
      }
    }
  }
}