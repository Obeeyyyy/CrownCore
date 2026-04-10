---
description: Everything you need to know about our license system and how to use it.
---

# Our License System

{% hint style="info" %}
Should any questions regarding the license system come up. Join our [Discord](https://discord.com/invite/bJQRV5GeZg) and ask for help.
{% endhint %}

## General Information <a href="#general-information" id="general-information"></a>

* You **do not need a port** to whitelist a server IP address.
* Some game server hosts do **not show your real IP address**.\
  In this case, please contact us so we can whitelist your server for you.
* An **invalid license key does NOT disable** our plugins.\
  If a plugin is disabled, it has another cause.
* You are **not allowed to share** your license key with other people or projects.

## How do I get my license key? <a href="#how-do-i-get-my-license-key" id="how-do-i-get-my-license-key"></a>

Open a **license ticket** in our [Discord](https://discord.com/invite/bJQRV5GeZg) and provide the following information:

* Your BuiltByBit username
* The plugin you purchased

## Where do I find my license key? <a href="#where-do-i-find-my-license-key" id="where-do-i-find-my-license-key"></a>

Use the `/license` command in our [Discord](https://discord.com/invite/bJQRV5GeZg).\
You will find your license key in the embed message that is sent.

![Image](https://wiki.obeeyyyy.de/assets/images/license-1-8b5c68c35af14af0744c6d381d90222d.png)

If you do not have a key yet, create a license ticket and ask for one.

### How do I whitelist my server on my license key? <a href="#how-do-i-whitelist-my-server-on-my-license-key" id="how-do-i-whitelist-my-server-on-my-license-key"></a>

1. Use `/license` in our [Discord](https://discord.com/invite/bJQRV5GeZg).
2. Click the **Manage Servers** button.
3.  Enter your server’s **IP address** (not a domain).

    An IP address looks like this:\
    `80.166.4.321`
4.  If you want to whitelist multiple IP addresses, separate them with a comma:

    `80.166.4.321,80.166.4.322`
5. Click **Save** and reload the plugin. A server restart is not required.

### My license key is not working <a href="#my-license-key-is-not-working" id="my-license-key-is-not-working"></a>

First, check if the plugin is enabled.

* If it is **not enabled**, continue here:\
  [How to install a plugin 101](https://wiki.obeeyyyy.de/docs/guides/how-to-install-a-plugin-101)

Then reload the plugin and check the console output.

* If you **do not see any message** stating that the license key is invalid, then your license key is working.
* If you **do see a message**, check the reason and continue below.

#### How do i reset my license key ? <a href="#how-do-i-reset-my-license-key" id="how-do-i-reset-my-license-key"></a>

* To reset your license key, you will need to use the /license command in our [Discord](https://discord.com/invite/bJQRV5GeZg).
* Then press the "**Reset License**" button.
* Confirm the action by typing "**YES**" and press "**Submit**".
* This will generate a new license key for you.

#### Reasons why your license key might not work <a href="#reasons-why-your-license-key-might-not-work" id="reasons-why-your-license-key-might-not-work"></a>

**Invalid server**

This means your server is **not whitelisted** on your license key.

Follow the instructions above to whitelist your server.

Make sure you are using the **server IP address**, not your panel URL or domain.

**Invalid key**

This means the license key found in your `config.yml` is **incorrect**.

Retrieve your license key from our [Discord](https://discord.com/invite/bJQRV5GeZg) again and update the value in the config.

#### Video Guide <a href="#video-guide" id="video-guide"></a>

* Check the video guide [here](https://www.youtube.com/watch?v=MScahBi-tms).
