#!/usr/bin/env python
import sys
import Skype4Py

chatMsg = sys.argv[1]
chatName = '#duane.homick/$ahsanziauddin;7c7ae8bee1db7a81' if len(sys.argv) < 3 else sys.argv[2]
try:
	skype = Skype4Py.Skype()
	skype.Attach()
except:
	print "Error Initing/Attaching, Exiting"
	sys.exit(0)
for chat in skype.Chats:
	if chat.Name == chatName:
		chat.SendMessage(chatMsg)

