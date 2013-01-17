#!/bin/sh

# usage: 
# ./scripts/create-lib.sh


# archive all classes

# jar cvf libmanet.jar -C ./bin/classes .


# selectively archive classes

cd ./bin/classes

jar cvf libmanet.jar \
'./android/adhoc/manet/service/CircularStringBuffer.class' \
'./android/adhoc/manet/service/LogObserver.class' \
'./android/adhoc/manet/service/ManetObserver.class' \
'./android/adhoc/manet/service/ManetHelper.class' \
'./android/adhoc/manet/service/ManetParser.class' \
'./android/adhoc/manet/service/ManetHelper$IncomingHandler.class' \
'./android/adhoc/manet/service/ManetHelper$ManetBroadcastReceiver.class' \
'./android/adhoc/manet/service/ManetHelper$ManetServiceConnection.class' \
'./android/adhoc/manet/service/routing/Edge.class' \
'./android/adhoc/manet/service/routing/Node.class' \
'./android/adhoc/manet/service/routing/Node$UpdatableHashSet.class' \
'./android/adhoc/manet/service/routing/OlsrProtocol.class' \
'./android/adhoc/manet/service/routing/SimpleProactiveProtocol.class' \
'./android/adhoc/manet/service/system/ManetConfig.class' \
'./android/adhoc/manet/service/system/ManetConfig$'* \
'./android/adhoc/manet/service/system/DeviceConfig.class' \
'./android/adhoc/manet/service/system/CoreTask.class' \
'./android/adhoc/manet/service/system/CoreTask$'* \
'./android/adhoc/manet/service/core/ManetService.class' \
'./android/adhoc/manet/service/core/ManetService$AdhocStateEnum.class'

mv libmanet.jar ../..

cd ../..


# copy jar into other project(s)
# copy will fail if destination dir does not exist

# cp libmanet.jar ../../android-manet-logger/AndroidManetLogger/libs

