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
            description("Git branch to release.")
        }
        choiceParam('RELEASE_TYPE', ['Stable', 'M', 'RC'], 'Stable release \nMilestone  \nRelease Candidate')
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
        buildName('${GIT_PROJECT} ${GIT_BRANCH} ${RELEASE_TYPE} #${BUILD_NUMBER}')
    }
    steps {
        shell('/usr/bin/git config --global user.email "mathieu_bretaud@carrefour.com"\n' +
                '/usr/bin/git config --global user.name "Mathieu Bretaud"\n' +
                '\n' +
                '/usr/bin/git checkout ${GIT_BRANCH}\n' +
                '\n' +
                'echo \'BUILD_VERSION=0.0.1\' > version.mk' +
                '\n' +
                '/usr/bin/git branch --set-upstream ${GIT_BRANCH} origin/${GIT_BRANCH}\n' +
                '\n' +
                'export ACTUAL_VERSION=0.0.1\n' +
                '\n' +
                'export NEXT_VERSION=0.0.2\n' +
                'export RELEASE_VERSION=0.0.2\n' +
                '\n' +
                '/usr/bin/git commit -am "prepare for release $RELEASE_VERSION"\n' +
                '\n' +
                'export http_proxy=${DATASCIENCE_HTTP_PROXY}\n' +
                'export https_proxy=${DATASCIENCE_HTTPS_PROXY}\n' +
                'export ftp_proxy=${DATASCIENCE_FTP_PROXY}\n' +
                'export no_proxy=${DATASCIENCE_NO_PROXY}\n' +
                '\n' +
                'export TAG_VERSION="v$RELEASE_VERSION"\n' +
                '\n' +
                '/usr/bin/git checkout -b release/$TAG_VERSION\n' +
                '/usr/bin/git tag $TAG_VERSION\n' +
                '/usr/bin/git push -uf origin $TAG_VERSION\n' +
                '\n' +
                '/usr/bin/git checkout ${GIT_BRANCH}\n' +
                '/usr/bin/git commit -am "Setting version to $NEXT_VERSION"\n' +
                '\n' +
                '/usr/bin/git push -uf origin $GIT_BRANCH')
    }
}
