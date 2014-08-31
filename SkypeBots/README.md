========================
SETUP INSTRUCTIONS
======================

First Clone the Repo at : https://github.com/awahlig/skype4py.git

CD into the folder and run
sudo arch -i386 /usr/bin/python2.7 setup.py build
sudo arch -i386 /usr/bin/python2.7 setup.py install

(Only Python 2.x is allowed for some reason breaks with 3.x)


To run the skype bot you need to have skype already running and you need to be signed in
To run a Bot program, from the command line run :
arch -i386 /usr/bin/python2.7 <bot_file_name> <arguments>

arch -i386 /usr/bin/python2.7 is important since the Skype Bot only runs on 32 Bit and crashes with Segfault on 64 Bit


==================
Git Hook
==================

To create a web hook run this command

If using Github corp change url from api.github.com to https://github-ca.corp.zynga.com/api/v3

```
  curl -i -u :github_user_name \
  -X 'POST' \
  'https://api.github.com/repos/:owner/:repo/hooks' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "web",
    "active": true,
    "events": ["push", "pull_request", "issues", "issue_comment", "commit_comment", "pull_request", "pull_request_review_comment"],
    "config": {
      "url": :url_to_post?chatName=:skype_chat_to_post_to,
      "content_type": "json"
    }
  }'
```



