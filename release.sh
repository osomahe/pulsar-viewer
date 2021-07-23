#!/bin/bash
type=$1
if [[ $type != "major" ]] && [[ $type != "minor" ]] && [[ $type != "patch" ]]; then
    echo "Type of release must be set(major, minor, patch). e.g. ./release.sh minor"
    exit 1
fi

version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "version $version"

versionMajor=$(echo $version | cut -d. -f1)
versionMinor=$(echo $version | cut -d. -f2)
versionPatchAll=$(echo $version | cut -d. -f3)
versionPatch=$(echo $versionPatchAll | cut -d- -f1)


if [[ $type = "major" ]]; then
    if [[ $versionMinor = 0 ]] && [[ $versionPatch = 0 ]]; then
        newMajor=$versionMajor
    else
       newMajor=$((versionMajor+1))
    fi
    newMinor=0
    newPatch=0
fi
if [[ $type = "minor" ]]; then
    if [[ $versionPatch = 0 ]]; then
        newMinor=$versionMinor
    else
       newMinor=$((versionMinor+1))
    fi
    newMajor=$versionMajor
    newPatch=0
fi
if [[ $type = "patch" ]]; then
    newMajor=$versionMajor
    newMinor=$versionMinor
    newPatch=$versionPatch
fi


newVersion="${newMajor}.${newMinor}.${newPatch}"
newSnapshot="${newMajor}.${newMinor}.$((newPatch+1))-SNAPSHOT"
echo "new version $newVersion"
echo "new snapshot version $newSnapshot"

mvn versions:set -DnewVersion=$newVersion versions:commit
git add pom.xml
git commit -m "move to release version $newVersion"
git tag -a "$newVersion" -m "version $newVersion"


mvn versions:set -DnewVersion=$newSnapshot versions:commit
git add pom.xml
git commit -m "move to snaphost $newSnapshot"

git push origin $newVersion
git push origin main
