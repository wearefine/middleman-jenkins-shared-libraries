# middleman-jenkins-shared-libraries

Deploying your middleman site on every change should be a smooth process. With this pipeline you can deploy to your repo and let Jenkins take care of building and deploying.

## Prerequisites

If you're new to Jenkins pipelines you should go read the [documentation](https://jenkins.io/doc/book/pipeline/) before proceeding to get a sense for what to expect using this code. The rest of the setup process will assume you have basic knowledge of Jenkins or CI/CD jobs in general.

OS
  - rvm installed in the jenkins user
  - git
  - build-essential

Jenkins
  - Version: > 2.7.3 - tested on (2.19.4 LTS)
  
Plugins
  - slack
  - pipeline (workflow-aggregator)
  - git
  - timestamper
  - credentials
  - sshagent
  - nodejs

Scripts Approval
- When the job runs the first time you will need to work through allowing certain functions to execute in the groovy sandbox. This is normal as not all high use groovy functions are in the default safelist but more are added all the time.

### Manage with Puppet
The following modules work great to manage a Jenkins instance.

- puppetlabs/apache
- rtyler/jenkins

## Jenkinsfile

``` groovy
middlemanApp {
  RUBY_VERSION = "ruby-x.x.x"
  RUBY_GEMSET = "app-name"
  DEPLOY =  ['master': 'scp -P <port> -rp build/ <username>@<hostname>:<deploy directory>', 'stage': '', 'dev': '', ... ]
  SSH_AGENT_ID = 'ssh-login-key'
  NODE_INSTALL_NAME = 'lts/boron'
  SLACK_CHANNEL = '#deploys'
  DEBUG = 'false'
}
```

### Required Parameters

- **RUBY_VERSION:** Ruby version to use. [String]
- **RUBY_GEMSET:** Name of the gemset to create. [String]
- **DEPLOY:** This is a map of branch and script key value pairs [Map] See [below](#Deploy Configuration) for more details
- **NODE_INSTALL_NAME:** The Nodejs plugin uses names to identify installs in Jenkins, enter that value here [String]

### Optional Parameters

- **SSH_AGENT_ID:** If you need to SCP files onto a machine then you need to add the sshagent plugin and give the auth ID here. [String]
- **SLACK_CHANNEL:** Specify the Slack channel to use for notifications. [String] Default: #deploys
- **DEBUG:** Turn off Slack notifications and turn on more console output. [String] Default: false

## Deploy Configuration

The deploy step allows for a lot of freedom and flexibility. Since middleman is just building a static site there are many ways that you may wish to deploy the code. The deploy param map listed below gives an example.

```groovy
DEPLOY =  [
  'master': 'scp -P <port> -rp build/ <username>@<hostname>:<deploy directory>', 
  '<branch>': '<action>'
]
```

I went ahead and left the first item in the map the same as above but changed the second to include explanations of the values. The deploy step in the code will compare the checked out branch name with the branch (key) value from the map. If they match the action is executed. If they don't match the build continues. The action uses `rvm()` wrapper function so no matter if you want to use middleman deploy or just a plain scp command the environment will be the same as the other job steps.

## [Changelog](CHANGELOG.md)

## [MIT License](LICENSE)

