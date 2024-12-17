#!/bin/sh
ver=`cat ./finalCampaign/launcher/current`
java -jar ./finalCampaign/launcher/$ver/launcher.jar "$@"