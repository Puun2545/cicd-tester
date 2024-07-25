def checkDiff(cron, jobPath) {
    def jobName = jobPath
    def cronParam = cron

    // Get the destination job's cron configuration
    def destJob = Jenkins.instance.getItemByFullName(jobName)
    if (destJob == null) {
        error "Destination job not found: ${jobName}"
    } else {
        echo "Destination job found: ${jobName}"
    }

    def triggers = destJob.getTriggers()
    echo "Triggers found: ${triggers.size()}"

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
        echo "Same as ${cron}"
        return True
    } else {
        echo "Differences"
        return False
    }

}