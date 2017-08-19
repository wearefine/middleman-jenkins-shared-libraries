#!/usr/bin/env groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    if (!config.DEBUG){
      config.DEBUG = 'false'
      env.DEBUG = 'false'
    }
    if (!config.SLACK_CHANNEL){
      config.SLACK_CHANNEL = '#deploys'
    }
    if (!config.RUBY_VERSION){
      error 'RUBY_VERSION is required'
    } else {
      env.RUBY_VERSION = config.RUBY_VERSION
    }
    if (!config.RUBY_GEMSET){
      error 'RUBY_GEMSET is required'
    } else {
      env.RUBY_GEMSET = config.RUBY_GEMSET
    }
    if (!config.DEPLOY){
      error 'DEPLOY is required'
    }
    if (!config.NODE_INSTALL_NAME) {
      error 'NODE_INSTALL_NAME is required so the asset compile works properly'
    }

    node {
      timestamps {
        if (config.DEBUG == 'false') {
          mmSlack(config.SLACK_CHANNEL)
        }

        nodejs(nodeJSInstallationName: config.NODE_INSTALL_NAME) {
          try {
            stage('Checkout') {
              checkout scm
              currentBuild.result = 'SUCCESS'
            }
          } catch(Exception e) {
            currentBuild.result = 'FAILURE'
            if (config.DEBUG == 'false') {
              mmSlack(config.SLACK_CHANNEL)
            }
            throw e
          }

          try {
            stage('Install Dependancies'){
              milestone label: 'Install Dependancies'
              retry(2) {
                mmRvm('bundle install')
              }
              currentBuild.result = 'SUCCESS'
            }
          } catch(Exception e) {
            currentBuild.result = 'FAILURE'
            if (config.DEBUG == 'false') {
              mmSlack(config.SLACK_CHANNEL)
            }
            throw e
          }

          try {
            stage('Build'){
              milestone label: 'Build'
              mmRvm('middleman build')
              currentBuild.result = 'SUCCESS'
            }
          } catch(Exception e) {
            currentBuild.result = 'FAILURE'
            if (config.DEBUG == 'false') {
              mmSlack(config.SLACK_CHANNEL)
            }
            throw e
          }

          try {
            stage('Deploy'){
              milestone label: 'Deploy'
              retry(2) {
                mmDeploy(env.BRANCH_NAME, config.SSH_AGENT_ID, config.DEPLOY)
              }
              currentBuild.result = 'SUCCESS'
            }
          } catch(Exception e) {
            currentBuild.result = 'FAILURE'
            if (config.DEBUG == 'false') {
              mmSlack(config.SLACK_CHANNEL)
            }
            throw e
          }
          if (config.DEBUG == 'false') {
            mmSlack(config.SLACK_CHANNEL)
          }
      } // nodejsRuntime
    } // timestamps
  } // node
}
