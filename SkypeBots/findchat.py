import sys
import Skype4Py

try:
	skype = Skype4Py.Skype()
	skype.Attach()
except:
	print "Error Initing/Attaching, Exiting"
	sys.exit(0)
for chat in skype.Chats:
	print chat.Name + " :: " + chat.Topic
