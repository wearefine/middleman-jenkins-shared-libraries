#!/usr/bin/env groovy

@NonCPS
def call(String branch, String agentId, Map deploy) {
  for ( e in deploy ) {
    if (e.key == branch){
      if (agentId) {
        sshagent([agentId]) {
          rvm(e.value)
        }
      } else {
        rvm(e.value)
      }
    }
  }
}