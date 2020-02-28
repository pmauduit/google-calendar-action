#!/bin/bash

export BOT_TOKEN=$1
export CHANNEL_NAME=$2
export FAILURE=$3
export MESSAGE=$4
export RAWDESC=$5
export URL=$6

# args:
#    - ${{ inputs.OAUTH_CLIENT_ID }}
#    - ${{ inputs.OAUTH_CLIENT_SECRET }}
#    - ${{ inputs.OAUTH_ACCESS_TOKEN }}
#    - ${{ inputs.OAUTH_REFRESH_TOKEN }}
#    - ${{ inputs.GCAL_ID }}
#    - ${{ inputs.GCAL_EVENT_SUMMARY }}
#    - ${{ inputs.GCAL_EVENT_DESCRIPTION }}

rm -f /gcal.properties
echo OAUTH_CLIENT_ID=$1 >> /gcal.properties
echo OAUTH_CLIENT_SECRET=$2 >> /gcal.properties
echo OAUTH_ACCESS_TOKEN=$3 >> /gcal.properties
echo OAUTH_REFRESH_TOKEN=$4 >> /gcal.properties
echo GCAL_ID=$5 >> /gcal.properties

# GCAL_EVENT_START_TIME is stored into /github/workspace/start (relative to the actions container)
#
# with this kind of "run actions":
#    - name: Saving start date
#      run: |
#        date +%s%3N > start

export GCAL_EVENT_SUMMARY=$6
export GCAL_EVENT_DESCRIPTION=$7
export GCAL_EVENT_START_TIME=$( cat /github/workspace/start )
export GCAL_EVENT_END_TIME=$( date +%s%3N )

# Avoid gh actions to pollute the default container env

export JAVA_HOME=/opt/java/openjdk


groovy -Dgrape.root=/grapes /publish.groovy
