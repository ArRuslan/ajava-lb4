#!/bin/bash

SESS_NAME="nure-lw4-chat"
WORK_DIR=$(dirname "$0")
SERVER_STARTED_TEXT="Starting server"
CLIENT_STARTED_TEXT="Server address"

mkdir $WORK_DIR/target/classes/ 2>/dev/null
javac -sourcepath $WORK_DIR/src/main/java -d $WORK_DIR/target/classes $WORK_DIR/src/main/java/ua/nure/jfm/task4/server/Main.java
javac -sourcepath $WORK_DIR/src/main/java -d $WORK_DIR/target/classes $WORK_DIR/src/main/java/ua/nure/jfm/task4/client/Main.java
javac -sourcepath $WORK_DIR/src/main/java -d $WORK_DIR/target/classes $WORK_DIR/src/main/java/ua/nure/jfm/task4/chat/Main.java
javac -sourcepath $WORK_DIR/src/main/java -d $WORK_DIR/target/classes $WORK_DIR/src/main/java/ua/nure/jfm/task4/shutdown/Main.java
cp $WORK_DIR/src/main/resources/* $WORK_DIR/target/classes/

tmux has-session -t $SESS_NAME 2>/dev/null
if [ $? = 0 ]; then
  tmux kill-session -t $SESS_NAME
fi

echo "Starting tmux session..."

tmux new-session -d -s $SESS_NAME -n "Server"
tmux send-keys -t $SESS_NAME "java -cp $WORK_DIR/target/classes ua.nure.jfm.task4.server.Main" Enter

echo "Waiting for server to start..."
until tmux capture-pane -pJ -S-10 -t "Server" | grep "$SERVER_STARTED_TEXT" >/dev/null; do :; done

for i in 1 2 3 4
do
  echo "Starting client $i..."
  tmux new-window -t $SESS_NAME -n "Client $i"
  tmux send-keys -t $SESS_NAME "java -cp $WORK_DIR/target/classes ua.nure.jfm.task4.client.Main" Enter
  until tmux capture-pane -pJ -S-10 -t "Client $i" | grep "$CLIENT_STARTED_TEXT" >/dev/null; do :; done

  tmux send-keys -t $SESS_NAME "127.0.0.1" Enter
  tmux send-keys -t $SESS_NAME "11111" Enter
  tmux send-keys -t $SESS_NAME "test$i" Enter
  tmux send-keys -t $SESS_NAME "123456" Enter
done

#for i in {5..99}
for i in 5 6 7 8
do
  echo "Starting chat $i..."
  tmux new-window -t $SESS_NAME -n "Chat $i"
  tmux send-keys -t $SESS_NAME "java -cp $WORK_DIR/target/classes ua.nure.jfm.task4.chat.Main" Enter
  until tmux capture-pane -pJ -S-10 -t "Chat $i" | grep "$CLIENT_STARTED_TEXT" >/dev/null; do :; done

  tmux send-keys -t $SESS_NAME "127.0.0.1" Enter
  tmux send-keys -t $SESS_NAME "11111" Enter
  tmux send-keys -t $SESS_NAME "test$i" Enter
  tmux send-keys -t $SESS_NAME "123456" Enter
done

echo "Starting shutdown..."
tmux new-window -t $SESS_NAME -n "Shutdown"
tmux send-keys -t $SESS_NAME "java -cp $WORK_DIR/target/classes ua.nure.jfm.task4.shutdown.Main" Enter
