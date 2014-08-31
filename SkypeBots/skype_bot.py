#!/usr/bin/env python
import sys
import Skype4Py
import subprocess
import urllib2
import json
import random
import thread
import time
import HTMLParser
import web
import traceback


urls = ('/.*', 'GitHook')

app = web.application(urls, globals())

class SkypeBot:
	# Singleton Instance
	_instance = None
	def __new__(cls, *args, **kwargs):
		if not cls._instance:
			cls._instance = super(SkypeBot, cls).__new__(cls, *args, **kwargs)
		return cls._instance

	def __init__(self):
		try:
			self.skypeClient = Skype4Py.Skype()
			self.skypeClient.Attach()
		except:
			print "Error Initing/Attaching, Exiting"
			sys.exit(0)

	def startJenkinsJob(self, chat, clComm, msgToPostAfterCompletetion):
		chat.SendMessage("Running : " + (' '.join(clComm)))
		p = subprocess.call(clComm, stdout=subprocess.PIPE)
		chat.SendMessage(msgToPostAfterCompletetion)

	def handle_ping(self, msg, status):
		msg.Chat.SendMessage("Relax, I'm still here!")

	def handle_cat(self, msg, status):
		p = subprocess.Popen(['curl', '-sL' , '-w', '"%{url_effective}\\n"', 'http://thecatapi.com/api/images/get?api_key=MTAwOTE', '-o', '/dev/null'], stdout=subprocess.PIPE)
		out, err = p.communicate()
		msg.Chat.SendMessage(out.strip(' \t\n\r')[1:-1])

	def handle_xkcd(self, msg, status):
		data = json.loads(urllib2.urlopen("http://xkcd.com/info.0.json").read())
		comic = random.randint(1, data['num'])
		msg.Chat.SendMessage("http://xkcd.com/" + `comic` + "/")

	def handle_chuck(self, msg, status):
		try:
			parts = msg.Body.strip().split()
			url = "http://api.icndb.com/jokes/random"
			if len(parts) > 1:
				url = url + "?limitTo=[" + ','.join(parts[1:]) + "]"
			data = json.loads(urllib2.urlopen(url).read())
			msg.Chat.SendMessage(HTMLParser.HTMLParser().unescape(data['value']['joke']))
		except:
			print "Error in bot ", sys.exc_info()[0]
			msg.Chat.SendMessage("Don't you have anything better to do")

	def handle_devansh(self, msg, status):
		try:
			parts = msg.Body.strip().split()
			url = "http://api.icndb.com/jokes/random?firstName=Devansh&lastName=Gupta"
			if len(parts) > 1:
				url = url + "&limitTo=[" + ','.join(parts[1:]) + "]"
			data = json.loads(urllib2.urlopen(url).read())
			msg.Chat.SendMessage(HTMLParser.HTMLParser().unescape(data['value']['joke']))
		except:
			print "Error in bot ", sys.exc_info()[0]
			msg.Chat.SendMessage("Don't you have anything better to do")

	def handle_build(self, msg, status):
		msgBody = msg.Body
		parts = msgBody.strip().split()
		partsLen = len(parts)
		if partsLen <= 1 or parts[1] == 'help':
			msg.Chat.SendMessage("build <job_name> <key_1> <value_1> .... <key_n> <value_n>")
		else:
			clComm = ['java', '-jar' , 'jenkins-cli.jar', '-s', 'http://10.101.51.58:8080/', 'build', parts[1], '-s']
			if partsLen > 2 and partsLen % 2 == 0:
				i = 2
				while i < partsLen:
					clComm.append('-p')
					clComm.append(parts[i] + '=' + parts[i + 1])
					i += 2
			msg.Chat.SendMessage("<<<<<<<<<<<<<<< Starting Job " + parts[1] + " <<<<<<<<<<<<<<<")
			try:
				thread.start_new_thread(self.startJenkinsJob, (msg.Chat, clComm, ">>>>>>>>>>>>>>> Finished Job " + parts[1] + " >>>>>>>>>>>>>>>"))
			except:
				msg.Chat.SendMessage("Error in Job ", parts[1])

	def handle_jenkins(self, msg, status):
		msgBody = msg.Body
		clComm = ['java', '-jar' , 'jenkins-cli.jar', '-s', 'http://10.101.51.58:8080/']
		parts = msgBody.strip().split()
		clComm = clComm + parts[1:]
		msg.Chat.SendMessage("<<<<<<<<<<<<<<< Starting Custom Job <<<<<<<<<<<<<<<")
		try:
			thread.start_new_thread(self.startJenkinsJob, (msg.Chat, clComm, ">>>>>>>>>>>>>>> Finished Custom Job >>>>>>>>>>>>>>>"))
		except:
			msg.Chat.SendMessage("Error in Job ", parts[1])

	def handleMessages(self, msg, status):
		if status == "SENDING":
			return

		if status == "READ":
			return

		msgBody = msg.Body

		if msgBody[0:1] != "!":
			return

		comm = msgBody.strip().split()[0][1:]

		commFunc = getattr(self, "handle_" + comm, None)

		if commFunc == None:
			msg.Chat.SendMessage("Unknown Command: " + comm)
		else:
			commFunc(msg, status)
			

	def startMonitoring(self):
		print "Starting Skype Bot"
		self.skypeClient.OnMessageStatus = self.handleMessages


	def startMonitoringOnNewThread(self):
		try:
			thread.start_new_thread(self.startMonitoring, ())
		except:
			print "Error in bot ", sys.exc_info()[0]

class GitHook:
	def POST(self):
		print "RECEIVED POST"
		data = web.data()
		queryString = web.input()
		try:
			self.handlePostJSON(json.loads(data), queryString.chatName)
		except:
			tb = traceback.format_exc()
			print tb

	def handlePostJSON(self, data, chatName):
		if data.get('comment', None) != None:
			sender = data['comment']['user']['login']
			comment = data['comment']['body']
			pr_link = data['comment']['_links']['pull_request']['href']
			pr = pr_link[pr_link.rfind('/') + 1:]
			comment_url = data['comment']['_links']['html']['href']
			self.send_msg(chatName, sender + " commented '" + comment + "' on Pull Request #" + pr + ". " + comment_url)
		elif data.get('commits', None) != None:
			headCommit = data['head_commit']['id']
			headCommitMessage = data['head_commit']['message']
			pusher = data['pusher']['name']
			numCommits = len(data['commits']) - 1
			ref = data['ref']
			branch = ref[ref.rfind('/') + 1:]
			repo = data['repository']['url']
			if numCommits > 0:
				self.send_msg(chatName, pusher + " pushed " + headCommit + " '" + headCommitMessage + "' and " + numCommits + " other commits to branch '" + branch + "' of " + repo)
			else:
				self.send_msg(chatName, pusher + " pushed " + headCommit + " '" + headCommitMessage + "' to branch '" + branch + "' of " + repo)
		elif data.get('action', "") == 'opened' and data.get('pull_request', None) != None:
			number = data['number']
			url = data['pull_request']['html_url']
			user = data['pull_request']['user']['login']
			repo = None
			try:
				repo = data['pull_request']['head']['repo']['name']
			except:
				repo = None
			if repo != None:
				self.send_msg(chatName, user + " opened Pull Request #" + str(number) + " of " + repo + " " + url)
			else:
				self.send_msg(chatName, user + " opened Pull Request #" + str(number) + " " + url)

	def send_msg(self, chatName, chatMsg):
		chats = SkypeBot().skypeClient.Chats
		for chat in chats:
			if chat.Name == chatName:
				chat.SendMessage(chatMsg)

if __name__ == '__main__':
	SkypeBot().startMonitoringOnNewThread()
	app.run()
