#!/bin/bash

# ##### User Editable section #####
# 
# Directory locations.  The script won't make these, it expects them to already be there.
SCRIPT_DIR="/home/nfdump"
IMPORT_DIR="/home/nfdump/import"
HDFS_DIR="/user/nfdump"
# File locations used during operations.  Don't put your own files in the locations chosen here, or they WILL be overwritten.
LOCKFILE="./import_lock"
FIFO_FILE="./import_fifo"
#
# ##### Don't edit below this line. #####

# Don't retain wildcards that don't expand to anything, for safety's sake.
shopt -s nullglob

# Acquire the lockfile or die.
cd "$SCRIPT_DIR"
lockfile-create -l -r 0 "$LOCKFILE" || exit 1

# If things go wrong now we have the lockfile, we need to take care of fixing that.  Sure, we can't trap 9, but if someone does send us that signal, they'd better clean it up for us.
trap "[ -f $SCRIPT_DIR/$LOCKFILE ] && /bin/rm -f $SCRIPT_DIR/$LOCKFILE" 0 1 2 3 13 15 

if [ ! -p "$FIFO_FILE" ]; then
    if [ -a "$FIFO_FILE" ]; then
        rm "$FIFO_FILE"
    fi
    mkfifo "$FIFO_FILE"
fi

if [ "$(ls -A $IMPORT_DIR)" ]; then
    # There are files to be imported.
    cd "$IMPORT_DIR"
    for f in *.nfcapd
    do
        hdfs fs -put "$FIFO_FILE" "$HDFS_DIR/$f.csv"& # Open the named pipe for reading.
        nfdump -r "$f" -o csv -q -N > "$FIFO_FILE" # Then write to it, blocking.
        rm "$f" #Then we're safe to delete.
    done
fi

cd "$SCRIPT_DIR"
rm -f "$LOCKFILE"