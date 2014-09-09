#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
set -u
bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

echo "========== preparing bayes data =========="

# configure
DIR=`cd $bin/../; pwd`
. "${DIR}/../bin/sparkbench-config.sh"
. "${DIR}/conf/configure.sh"

# path check
$HADOOP_EXECUTABLE dfs -rmr $INPUT_HDFS

# generate data
OPTION="-t bayes \
        -b ${BAYES_BASE_HDFS} \
        -n ${BAYES_INPUT} \
        -m ${NUM_MAPS} \
        -r ${NUM_REDS} \
        -p ${PAGES} \
        -class ${CLASSES} \
        -o sequence"
#        -x ${DICT_PATH} \

$HADOOP_EXECUTABLE jar ${DATATOOLS} HiBench.DataGen ${OPTION} 
result=$?
if [ $result -ne 0 ]
then
    echo "ERROR: Hadoop job failed to run successfully." 
    exit $result
fi

${SPARK_HOME}/bin/spark-submit --class Convert --master ${SPARK_MASTER} ${DIR}/prepare/target/scala-2.10/hibench-bayes-data-converter_2.10-1.0.jar ${HDFS_MASTER} ${INPUT_HDFS} ${PARALLEL}
