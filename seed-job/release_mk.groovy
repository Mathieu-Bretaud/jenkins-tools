def jobName = "ADMIN-release-mk"

def gitUrl = 'git@github.com:Mathieu-Bretaud/$GIT_PROJECT.git'

job(jobName) {
    parameters {
        stringParameterDefinition {
            name("GIT_PROJECT")
            defaultValue("")
            description("Git repository name. Exemple : data-catalog")
        }
        stringParameterDefinition {
            name("GIT_BRANCH")
            defaultValue("develop")
            description("Git branch with modifications.")
        }
        stringParameterDefinition {
            name("GIT_BRANCH_TO_PUSH")
            defaultValue("staging")
            description("Git branch to push.")
        }
    }
    environmentVariables {
        env('DISABLE_DATASCIENCE_DEV',"true")
    }
    logRotator {
        daysToKeep(3)
        numToKeep(3)
    }
    scm {
        git {
            remote {
                url(gitUrl)
            }
            branch('${GIT_BRANCH}')
            extensions {
                cleanBeforeCheckout()
                localBranch('${GIT_BRANCH}')
                wipeOutWorkspace()
            }
        }
    }
    wrappers {
        buildName('${GIT_PROJECT} ${GIT_BRANCH} ${GIT_BRANCH_TO_PUSH}')
    }
    steps {
        shell('/usr/bin/git config --global user.email "mathieu_bretaud@carrefour.com"\n' +
                '/usr/bin/git config --global user.name "Mathieu Bretaud"\n' +
                '\n' +
                '/usr/bin/git checkout ${GIT_BRANCH}\n' +
                '\n' +
                'export NEXT_VERSION=0.0.2\n' +
                'export RELEASE_VERSION=0.0.2\n' +
                '\n' +
                '/usr/bin/git commit -am "prepare for release $RELEASE_VERSION"\n' +
                '\n' +
                'export TAG_VERSION="v$RELEASE_VERSION"\n' +
                '\n' +
                '/usr/bin/git tag $TAG_VERSION\n' +
                '/usr/bin/git push origin ${GIT_BRANCH}:${GIT_BRANCH_TO_PUSH}\n')
    }
}
