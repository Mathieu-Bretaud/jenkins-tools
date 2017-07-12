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
    label('slave')
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
                'echo 0.0.1 > version.mk' +
                '\n' +
                '/usr/bin/git branch --set-upstream-to ${GIT_BRANCH} origin/${GIT_BRANCH}\n' +
                '\n' +
                'export ACTUAL_VERSION=`grep \'BUILD_VERSION=\' version.mk | sed \'s/BUILD_VERSION=//\'`\n' +
                'RELEASE_VERSION_COMPUTED=`echo $ACTUAL_VERSION | sed \'s/-SNAPSHOT//\'`\n' +
                'export NEXT_VERSION=$ACTUAL_VERSION\n' +
                '\n' +
                'case $RELEASE_TYPE in \n' +
                '                Stable)       \n' +
                '                  IFS=\'.\' read MAJOR MINOR BUGFIX <<<$RELEASE_VERSION_COMPUTED\n' +
                '                  export NEXT_VERSION=$MAJOR.`expr ${MINOR} + 1`.$BUGFIX-SNAPSHOT\n' +
                '                  export RELEASE_VERSION=$RELEASE_VERSION_COMPUTED\n' +
                '                  ;;\n' +
                '                RC|M)\n' +
                '                  if [[ `/usr/bin/git tag | grep "$RELEASE_VERSION_COMPUTED$"` ]]; then\n' +
                '                  \t echo "we cant release a RC/M release when a Stable release exists"\n' +
                '                  \t exit 1\n' +
                '                   \n' +
                '                  fi\n' +
                '                  if [[ "$RELEASE_TYPE" == "M" && `/usr/bin/git tag | grep $RELEASE_VERSION_COMPUTED-RC` ]]; then\n' +
                '                  \t echo "we cant release a Milestone when a Release Candidate exists"\n' +
                '                  \t exit 1\n' +
                '                   \n' +
                '                  fi\n' +
                '                  ACTUAL_RELEASE_NUMBER=`/usr/bin/git tag | grep $RELEASE_VERSION_COMPUTED-$RELEASE_TYPE | sed -ne "s/.*$RELEASE_TYPE//gp" | sort -nr | head -1`\n' +
                '                  echo ACTUAL_RELEASE_NUMBER=$ACTUAL_RELEASE_NUMBER\n' +
                '                  RELEASE_VERSION=$RELEASE_VERSION_COMPUTED-$RELEASE_TYPE`expr ${ACTUAL_RELEASE_NUMBER:-0} + 1`\n' +
                '                  ;;\n' +
                '                *)              \n' +
                '          esac \n' +
                '\n' +
                'export NEXT_VERSION_LINE="NEXT_VERSION=$NEXT_VERSION"\n' +
                'export BUILD_VERSION_LINE="BUILD_VERSION=$RELEASE_VERSION"\n' +
                '\n' +
                '/bin/bash -c \'( cat version.mk | grep -vE "BUILD_VERSION=" | grep -vE "NEXT_VERSION="; echo -e "$BUILD_VERSION_LINE\\n$NEXT_VERSION_LINE" ) > version.mk\'\n' +
                '\n' +
                '/usr/bin/git commit -am "prepare for release $RELEASE_VERSION"\n' +
                '\n' +
                '\n' +
                '\n' +
                '\n' +
                '\n' +
                'export http_proxy=${DATASCIENCE_HTTP_PROXY}\n' +
                'export https_proxy=${DATASCIENCE_HTTPS_PROXY}\n' +
                'export ftp_proxy=${DATASCIENCE_FTP_PROXY}\n' +
                'export no_proxy=${DATASCIENCE_NO_PROXY}\n' +
                '\n' +
                'make package-and-publish\n' +
                '\n' +
                '\n' +
                '\n' +
                '\n' +
                '\n' +
                'export TAG_VERSION="v$RELEASE_VERSION"\n' +
                '\n' +
                '/usr/bin/git checkout -b release/$TAG_VERSION\n' +
                '/usr/bin/git tag $TAG_VERSION\n' +
                '/usr/bin/git push -uf origin $TAG_VERSION\n' +
                '\n' +
                '\n' +
                'export DEV_VERSION_LINE="BUILD_VERSION=$NEXT_VERSION"\n' +
                '\n' +
                '/bin/bash -c \'( cat version.mk | grep -vE "BUILD_VERSION=" | grep -vE "NEXT_VERSION="; echo -e "$DEV_VERSION_LINE" ) > version.mk\'\n' +
                '\n' +
                '\n' +
                '/usr/bin/git checkout ${GIT_BRANCH}\n' +
                '/usr/bin/git commit -am "Setting version to $NEXT_VERSION"\n' +
                '\n' +
                '/usr/bin/git push -uf origin $GIT_BRANCH')
    }
}
