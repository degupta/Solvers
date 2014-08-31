PORT=$1
if [[ -n "$PORT" ]]
then
	echo "Running on port " $PORT
	arch -i386 /usr/bin/python2.7 skype_bot.py $PORT
else
	echo "Running on port 8080"
	arch -i386 /usr/bin/python2.7 skype_bot.py
fi
