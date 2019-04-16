# Pigeon Plugins

![Heroku](https://heroku-badge.herokuapp.com/?app=pigeon-public&root=/pigeon/inputMessages)

Collection of open source Pigeon Plugins

This repository contains open source Pigeon Plugins, such as Self Test Plugin and SMSGlobal Plugin.

For more details refer to:
- [Pigeon Plugins Documentation](https://github.com/INFINITE-TECHNOLOGY/PIGEON/wiki/Plugins)
- [Pigeon Documentation](https://github.com/INFINITE-TECHNOLOGY/PIGEON/wiki/)

> ❗ This repository is fully Heroku ready! Just fork it, change/add plugins and configuration - and deploy as your own Heroku app!

## Try me now!

This repository is deployed as a demo Heroku app (`pigeon-public`).

Just open the below URL in your browser:

https://pigeon-public.herokuapp.com/pigeon/enqueue?source=browser&endpoint=GET_TO_SMTP&recipient=email@gmail.com&subject=Test123&text=Test1234&from=pigeon@i-t.io

This demo Heroku `pigeon-public` app asynchronously enqueues and sends a email.

* Replace `email@gmail.com` with your email (we will not save/share/store/disclose it, it is fully private)<br/>
* Only Gmail addresses are supported in this demo<br/>
* You can change also subject, text and from <br/>
* Check `spam` folder in your Gmail account<br/>
* Navigate through returned URLs to see message status and HTTP logs<br/>
* First time request may take up to 50 seconds, due to free Heroku dyno unidlying startup.
