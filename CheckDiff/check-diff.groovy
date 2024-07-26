pipeline {
    agent any

    parameters {
        string(name: 'CRON', defaultValue: '0 0 * * 1-7', description: 'Cron string to compare')
        string(name: 'JOB_DES_PATH', defaultValue: 'project-a/stop/stop-instance', description: 'Destination job full path to compare')
    }

    stages {
        stage('Compare Cron Strings') {
            steps {
                script {
                    def jobName = params.JOB_DES_PATH
                    def cronParam = params.CRON

                    // Get the destination job's cron configuration
                    def destJob = Jenkins.instance.getItemByFullName(jobName)

                    // Check if the destination job exists
                    if (destJob == null) {
                        error "Destination job not found: ${jobName}"
                    } else {
                        echo "Destination job found: ${jobName}"
                    }

                    // Get the triggers of the destination job
                    def triggers = destJob.getTriggers()
                    echo "Triggers found: ${triggers.size()}"

                    // Get the cron configuration of the destination job
                    def cronConfig = ''
                    triggers.each { trigger, descriptor ->
                        echo "Trigger class: ${trigger.getClass()}, Descriptor: ${descriptor.getClass()}"
                        if (descriptor instanceof hudson.triggers.TimerTrigger) {
                            cronConfig = descriptor.getSpec()
                            echo "Found cron trigger: ${cronConfig}"
                        }
                    }

                    if (cronConfig == '') {
                        error "No cron trigger found for the destination job: ${jobName}"
                    }

                    // Compare the cron parameter with the cron configuration
                    if (cronParam == cronConfig) {
                        echo "Same as ${params.CRON}"
                    } else {
                        echo "Differences"
                    }
                }
            }
        }
    }
}
