#!/bin/bash
# /etc/init.d/supoxy

### BEGIN INIT INFO
# Provides:   supoxy
# Required-Start: $local_fs $remote_fs
# Required-Stop:  $local_fs $remote_fs
# Should-Start:   $network
# Should-Stop:    $network
# Default-Start:  2 3 4 5
# Default-Stop:   0 1 6
# Short-Description:    supoxy server
# Description:    Init script for supoxy server, a proxy Server to SunnyPortal from SMA; with rolling logs
### END INIT INFO

# Based on https://github.com/Ahtenus/minecraft-init - Thanx guys!
# adapted to supoxy by ThetaGamma

# Loads config file

if [ -L $0 ]
then
	source `readlink -e $0 | sed "s:[^/]*$:supoxy_init_config:"`
else
	source `echo $0 | sed "s:[^/]*$:supoxy_init_config:"`
fi

if [ "$SERVICE" == "" ]
then
	echo "Couldn't load config file, please edit config.example and rename it to supoxy_init_config"
	logger -t supoxy-init "Couldn't load config file, please edit config.example and rename it to supoxy_init_config"
	exit
fi

ME=`whoami`
as_user() {
	if [ $ME == $USERNAME ] ; then
		bash -c "$1"
	else
		su $USERNAME -s /bin/bash -c "$1"
	fi
}

is_running() {
	# Checks for the supoxy servers screen session
	# returns true if it exists.
	pidfile=${SUPATH}/${SCREEN}.pid

	if [ -f "$pidfile" ]
	then
		pid=$(head -1 $pidfile)
		if ps ax | grep -v grep | grep ${pid} | grep "${SCREEN}" > /dev/null
		then
			return 0
		else
			if [ -z "$isInStop" ]
			then
				if [ -z "$roguePrinted" ]
				then
					roguePrinted=1
					echo "Rogue pidfile found!"
				fi
			fi
			return 1
		fi
	else
		if ps ax | grep -v grep | grep "${SCREEN} ${INVOCATION}" > /dev/null
		then
			echo "No pidfile found, but server's running."
			echo "Re-creating the pidfile."

			pid=$(ps ax | grep -v grep | grep "${SCREEN} ${INVOCATION}" | cut -f1 -d' ')
			check_permissions
			as_user "echo $pid > $pidfile"

			return 0
		else
			return 1
		fi
	fi
}

datepath() {
	# datepath path filending-to-check returned-filending

	# Returns an file path with added date between the filename and file ending.
	# $1 filepath (not including file ending)
	# $2 file ending to check for uniqueness
	# $3 file ending to return

	if [ -e $1`date +%F`$2 ]
	then
		echo $1`date +%FT%T`$3
	else
		echo $1`date +%F`$3
	fi
}

su_start() {
	servicejar=$SUPATH/$SERVICE
	if [ ! -f "$servicejar" ]
	then
		echo "Failed to start: Can't find the specified supoxy jar under $servicejar. Please check your config!"
		exit 1
	fi

	pidfile=${SUPATH}/${SCREEN}.pid
	check_permissions

	as_user "cd $SUPATH && screen -dmS $SCREEN $INVOCATION"
	as_user "screen -list | grep "\.$SCREEN" | cut -f1 -d'.' | head -n 1 | tr -d -c 0-9 > $pidfile"

	#
	# Waiting for the server to start
	#
	seconds=0
	until is_running
	do
		sleep 1
		seconds=$seconds+1
		if [[ $seconds -eq 5 ]]
		then
			echo "Still not running, waiting a while longer..."
		fi
		if [[ $seconds -ge 120 ]]
		then
			echo "Failed to start, aborting."
			exit 1
		fi
	done
	echo "$SERVICE is running."
}

su_command() {
	if is_running
	then
			as_user "screen -p 0 -S $SCREEN -X eval 'stuff \"$(eval echo $FORMAT)\"\015'"
	else
			echo "$SERVICE was not running. Not able to run command."
	fi
}

su_stop() {
	pidfile=${SUPATH}/${SCREEN}.pid
	#
	# Stops the server
	#
	echo "Stopping server..."
	#su_command "killall java" # might not work, do better!
	force_exit
	sleep 0.5
	#
	# Waiting for the server to shut down
	#
	seconds=0
	isInStop=1
	while is_running
	do
		sleep 1
		seconds=$seconds+1
		if [[ $seconds -eq 5 ]]
		then
			echo "Still not shut down, waiting a while longer..."
		fi
		if [[ $seconds -ge 120 ]]
		then
			logger -t supoxy-init "Failed to shut down server, aborting."
			echo "Failed to shut down, aborting."
			exit 1
		fi
	done
	as_user "rm $pidfile"
	unset isInStop
	is_running
	echo "$SERVICE is now shut down."
}

check_backup_settings() {
	case "$BACKUPFORMAT" in
		tar)
			COMPRESSCMD="tar -hcjf"
			STORECMD="tar -cpf"
			ARCHIVEENDING=".tar.bz2"
			STOREDENDING=".tar"
			EXCLUDEARG="-X "
			;;
		zip)
			COMPRESSCMD="zip -rq"
			STORECMD="zip -rq -0"
			ARCHIVEENDING=".zip"
			STOREDENDING=".zip"
			EXCLUDEARG="-x@"
			;;
		*)
			echo "$BACKUPFORMAT is not a supported backup format"
			exit 1
			;;
	esac
}

log_roll() {
	# Moves the logfiles and compresses that backup directory
	check_backup_settings
	path=`datepath $LOGPATH/logs_ $ARCHIVEENDING`
	as_user "mkdir -p $path"

	shopt -s extglob
	for FILE in $(ls $SUPATH/*.log)
	do
		as_user "cp $FILE $path"
		# only if previous command was successful
		if [ $? -eq 0 ]; then
			if [[ "$FILE" = @(*-+([0-9]).log) && "$FILE" = !(*-0.log) ]]
			# some mods already roll logs. remove all but the most recent file
			# which ends with -0.log
			then
				as_user "rm $FILE"
			else
			# truncate the existing log without restarting server
				as_user "cp /dev/null $FILE"
				as_user "echo \"Previous logs rolled to $path\" > $FILE"
			fi
		else
			echo "Failed to rotate log from $FILE into $path"
		fi
	done

	as_user "$COMPRESSCMD $path$ARCHIVEENDING $path"
	if [ $? -eq 0 ]; then
		as_user "rm -r $path"
	fi
}

su_whole_backup() {
	check_backup_settings
	echo "backing up entire setup into $WHOLEBACKUP"
	path=`datepath $WHOLEBACKUP/mine_`
	locationOfScript=$(dirname "$(readlink -e "$0")")
	as_user "mkdir -p $path"

	if [ -r "$locationOfScript/exclude.list" ]
	then
		echo "...except the following files and/or dirs:"
		cat $locationOfScript/exclude.list
		exclude="$EXCLUDEARG$locationOfScript/exclude.list"
	fi
	if [ "$COMPRESS_WHOLEBACKUP" ]
	then
		as_user "$COMPRESSCMD $path/whole-backup$ARCHIVEENDING $SUPATH $exclude"
	else
		as_user "$STORECMD $path/whole-backup$STOREDENDING $SUPATH $exclude"
	fi
}

su_update() {
	if is_running
	then
		echo "$SERVICE is running! Will not start update."
	else
		if check_update_vanilla
		then
			if [ -r "$SUPATH/supoxy_server.jar.update" ]
			then
				as_user "mv $SUPATH/supoxy_server.jar.update $SUPATH/$SU_JAR"
				echo "Thats it. Update of $SU_JAR done."
			else
				echo "Something went wrong. Couldn't replace your original $SU_JAR with supoxy_server.jar.update"
			fi
		else
			echo "Not updating $MB_JAR. It's not necessary"
			as_user "rm $SUPATH/supoxy_server.jar.update"
		fi

	fi
}
force_exit() {  # Kill the server running (messily) in an emergency
	echo ""
	echo "SIGINIT CALLED - FORCE EXITING!"
	pidfile=${SUPATH}/${SCREEN}.pid
	rm $pidfile
	echo "KILLING SERVER PROCESSES!!!"
		# Display which processes are being killed
		ps aux | grep -e 'java -jar SuPoxy.jar' | grep -v grep | awk '{print $2}' | xargs -i echo "Killing PID: " {}
		ps aux | grep -e "SCREEN -dmS supoxy_screen java" | grep -v grep | awk '{print $2}' | xargs -i echo "Killing PID: " {}
		ps aux | grep -e '/etc/init.d/supoxy' | grep -v grep | awk '{print $2}' | xargs -i echo "Killing PID: " {}

		# Kill the processes
		ps aux | grep -e 'java -jar SuPoxy.jar' | grep -v grep | awk '{print $2}' | xargs -i kill {}
		ps aux | grep -e "SCREEN -dmS supoxy_screen java" | grep -v grep | awk '{print $2}' | xargs -i kill {}
		ps aux | grep -e '/etc/init.d/supoxy' | grep -v grep | awk '{print $2}' | xargs -i kill {}

	exit 1
}

get_script_location() {
	echo $(dirname "$(readlink -e "$0")")
}

check_permissions() {
	as_user "touch $pidfile"
	if ! as_user "test -w '$pidfile'" ; then
		echo "Check Permissions. Cannot write to $pidfile. Correct the permissions and then excute: $0 status"
	fi
}

trap force_exit SIGINT

case "$1" in
	start)
		# Starts the server
		if is_running; then
			echo "Server already running."
		else
			su_start
		fi
		;;
	stop)
		# Stops the server
		if is_running; then
			su_stop
		else
			echo "No running server."
		fi
		;;
	restart)
		# Restarts the server
		if is_running; then
			echo "SERVER REBOOT IN 10 SECONDS!"
			su_stop
		else
			echo "No running server, starting it..."
		fi
		su_start
		;;
	backup)
		# Backup everything
		if is_running; then
			su_stop
			su_whole_backup
			su_start
		else
			su_whole_backup
		fi
		;;
	check-update)
		check_update_vanilla
		as_user "rm $SUPATH/supoxy_server.jar.update"
		;;
	update)
		#update supoxy.jar
		if is_running; then
			su_say "SERVER UPDATE IN 10 SECONDS."
			su_stop
			su_whole_backup
			su_update
			su_start
		else
			su_whole_backup
			su_update
		fi
		;;
	command)
		if is_running; then
			shift 1
			su_command "$*"
			echo "Sent command: $*"
		else
			echo "No running server to send a command to."
		fi
		;;
	log-roll)
		log_roll
		;;
	log)
		# Display server log using 'cat'.
		cat $SERVERLOG
		;;
	last)
		# Greps for last events
		# TODO wget server:8000/last
		;;
	status)
		# Shows server status
		if is_running
		then
			echo "$SERVICE is running."
		else
			echo "$SERVICE is not running."
		fi
		;;
	version)
		if is_running; then
			su_command version
			tac $SERVERLOG | grep -m 1 "This server is running"
		else
			echo "The server needs to be running to check version."
		fi
		;;
	screen)
		if is_running; then
			as_user "script /dev/null -q -c \"screen -rx $SCREEN\""
		else
		echo "Server is not running. Do you want to start it?"
		echo "Please put \"Yes\", or \"No\": "
		read START_SERVER
		case "$START_SERVER" in
			[Yy]|[Yy][Ee][Ss])
				su_start
				as_user "script /dev/null -q -c \"screen -rx $SCREEN\""
				;;
			[Nn]|[Nn][Oo])
				clear
				echo "Aborting startup!"
				sleep 1
				clear
				exit 1
				;;
			*)
				clear
				echo "Invalid input"
				sleep 1
				clear
				exit 1
				;;
		esac
		fi
		;;
	kill)
		WIDTH=`stty size | cut -d ' ' -f 2`            # Get terminal's character width
		pstree | grep MDSImporte | cut -c 1-${WIDTH}   # Chop output after WIDTH chars

		echo "Killing the server is an EMERGENCY procedure, and should not be used to perform a normal shutdown! Are you ABSOLUTELY POSITIVE this is what you want to do?"
		echo "Please put \"Yes\", or \"No\": "
		read KILL_SERVER
		case "$KILL_SERVER" in  # Determine which option was specified
			[Yy]|[Yy][Ee][Ss])      # If yes, kill the server
				echo "KILLING SERVER PROCESSES!!!"
				force_exit
				exit 1
				;;
			[Nn]|[Nn][Oo])  # If no, abort and exit 1
				echo "Aborting!"
				exit 1
				;;
			*)      # If anything else, exit 1
				echo "Error: Invalid Input!"
				exit 1
				;;
		esac
		;;
	help|--help|-h)
		echo "Usage: $0 COMMAND"
		echo
		echo "Available commands:"
		echo -e "   start \t\t Starts the server"
		echo -e "   stop \t\t Stops the server"
		echo -e "   kill \t\t Kills the server"
		echo -e "   restart \t\t Restarts the server"
		echo -e "   backup \t\t Backups the server"
		echo -e "   check-update \t Checks for updates of $SU_JAR"
		echo -e "   update \t\t Fetches the latest version of supoxy.jar server and Bukkit"
		echo -e "   log-roll \t\t Moves and compresses the logfiles"
		echo -e "   log \t\t\t Prints the server log"
		echo -e "   status \t\t Displays server status"
		echo -e "   version \t\t Displays version and then exits"
		echo -e "   links \t\t Creates nessesary symlinks"
		echo -e "   screen \t\t Shows the server screen"
		;;
	*)
		echo "No such command, see $0 help"
		exit 1
		;;
esac

exit 0
