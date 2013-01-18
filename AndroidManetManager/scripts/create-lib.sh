#!/bin/sh

# usage: 
# ./scripts/create-lib.sh


# archive all classes

# jar cvf libmanet.jar -C ./bin/classes .


# selectively archive classes

cd ./bin/classes

jar cvf libmanet.jar \
'./org/span/service/CircularStringBuffer.class' \
'./org/span/service/LogObserver.class' \
'./org/span/service/ManetObserver.class' \
'./org/span/service/ManetHelper.class' \
'./org/span/service/ManetParser.class' \
'./org/span/service/ManetHelper$IncomingHandler.class' \
'./org/span/service/ManetHelper$ManetBroadcastReceiver.class' \
'./org/span/service/ManetHelper$ManetServiceConnection.class' \
'./org/span/service/routing/Edge.class' \
'./org/span/service/routing/Node.class' \
'./org/span/service/routing/Node$UpdatableHashSet.class' \
'./org/span/service/routing/OlsrProtocol.class' \
'./org/span/service/routing/SimpleProactiveProtocol.class' \
'./org/span/service/system/ManetConfig.class' \
'./org/span/service/system/ManetConfig$'* \
'./org/span/service/system/DeviceConfig.class' \
'./org/span/service/system/CoreTask.class' \
'./org/span/service/system/CoreTask$'* \
'./org/span/service/core/ManetService.class' \
'./org/span/service/core/ManetService$AdhocStateEnum.class' \
'./org/span/service/legal/EulaHelper.class' \
'./org/span/service/legal/EulaHelper$'* \
'./org/span/service/legal/EulaObserver.class'

mv libmanet.jar ../..

cd ../..


# copy jar into other project(s)
# copy will fail if destination dir does not exist

# cp libmanet.jar ../../android-manet-logger/AndroidManetLogger/libs

