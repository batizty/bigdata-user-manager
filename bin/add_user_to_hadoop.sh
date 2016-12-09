#!/bin/sh

if [ $# -lt 1 ]; then
    echo "Not Enough Arguments"
    echo "Usage : ./add_user_to_hadoop.sh use_name [group_name]"
    exit -1
fi

user_name=$1

if [ $# -eq 2 ]; then
    group_name=$2
else
    group_name=$user_name
fi

#### Common Functions ####
function checkHDFS() {
    target_file="/user/$user_name"
    cmd="hadoop fs -test -e ${target_file}"
    `$cmd`
    result=$?
    if [ $result -eq 0 ]; then
        return 1
    else
        return 0
    fi
}

function init() {
    target_file="/user/$user_name"
    printInfo "init $target_file for user $user_name"
    runCommands "$hadoop_user_prefix hadoop fs -mkdir $target_file"
    ret=$?

    if [ $ret -ne 0 ]; then
        printError "init $target_file for user $user_name failed"
        return $ret
    fi

    printInfo "init $target_file ownership for user $user_name:$group_name"
    runCommands "$hadoop_user_prefix hadoop fs -chown -R $user_name:$group_name $target_file"
    ret=$?

    if [ $ret -ne 0 ]; then
        printError "init $target_file ownership for user $user_name:$group_name"
        return $ret
    fi

    return 0
}

function runCommands() {
    cmd=$1
    failed_times=0
    while [ $failed_times -lt 3 ]; do
        echo -e "run command : $cmd"
        `$cmd`
        if [ $? -eq 0 ]; then
            return 0
        else
			let $failed_times++
        fi
    done
    return -1
}

## Global Functions
function printInfo {
    date=`date -u`;
    echo "$date [INFO] : $1"
}

function printError {
    date=`date -u`
    echo "$date [ERROR] : $1"
}

printInfo "add hadoop user for $user_name:$group_name"

#### Setting Environment ####
export HADOOP_HOME="/usr/local/hadoop"
export HADOOP_CONF_DIR="$HADOOP_HOME/etc/hadoop"
source "${HADOOP_CONF_DIR}/hadoop-env.sh"

#### check hadoop HDFS have this user and group ####
printInfo "check user $user_name exists or not"
checkHDFS
RET=$?
#### if not exists, init files for this user and group ####
if [ $RET -eq 0 ]; then
    printInfo "user $user_name is not in HDFS and init for $user_name"
    init
    RET=$?
else
    printError "user $user_name is already in system"
fi
