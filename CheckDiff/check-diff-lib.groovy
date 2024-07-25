library 'my-shared-library@v0.0.2'

properties ([ 
    parameters ([
        string(name: 'CRON', defaultValue: '0 0 * * 1-7', description: 'Cron string to compare'),
        string(name: 'JOB_DES_PATH', defaultValue: 'project-a/stop/stop-instance', description: 'Destination job path to compare')
    ])
])

runpipeline ([
    CRON: params.CRON,
    JOB_DES_PATH: params.JOB_DES_PATH
]) { context -> 
    checkDiff(context)
}

def runpipeline(Map args, Closure stages) {
    node('master') {
        ansiColor('xterm') {
            timestamps(){
                timeout(time: 50, unit: 'MINUTES'){
                    skipDefaultCheckout(true)
                    try {
                        stages(args)
                    } catch (Exception err) {
                        throw err
                    } finally {
                        echo 'DONE'
                    }
                }
            }
        }
    }
}

def checkDiff(Map args) {
    stage('Chanck CRON Diff') {
        def diff = checkDiff(args.CRON, args.JOB_DES_PATH)
        if (diff) {
            echo "Same as ${args.CRON}"
        } else {
            echo "Differences"
        }
    }
}