Barchomat
=========

**Sir Barchable's Clash of Clans Proxy**

## What?

This is a little program that sits between your game of Clash and the server at Supercell, and watches the messages
that pass between them.

## Why?

Supercell doesn't provide an easy way for clan leaders to collect clan and enemy stats. If you want to track the 
performance of the 40+ players in your clan the paper and pencil approach is tedious. 

## What can it do?

Not a lot at the moment. The initial release just dumps stats as you view villages, and sums up stats for the clans 
you've scouted when your session ends.

    08:47:49 INFO  Eclipse
    08:47:49 INFO  DPS: 185, HP: 20490 (walls 52300)
    08:47:49 INFO  Troops: [lvl 4 Barbarian x 6, lvl 4 Archer x 4]
    08:47:49 INFO  Town Hall: Loot[g=1000, e=1000, de=0]
    08:47:49 INFO  Storage: Loot[g=17934, e=18540, de=0]
    08:47:49 INFO  Castle: Loot[g=0, e=0, de=0]
    08:47:49 INFO  Collectors: Loot[g=6520, e=9939, de=0]
    08:47:49 INFO  Total: Loot[g=25454, e=29479, de=0]
    08:47:49 INFO  After penalty of 10%: Loot[g=22908, e=26531, de=0]
    
    08:47:49 INFO  40 villages in Боги Войн with total Defense[hp=1832305, wallHp=13423500, dps=36391]
    
We're working on player stat collection and collation. Things will progress faster if you help out.    

## Do I have root my device/install your dodgy hack?

No, this is a completely separate program. No mods are required.

## Will I get banned for using this?

No, the proxy doesn't modify the traffic in any way. All it does is monitor the conversation that you're having with 
Supercell, so there's no way of telling that it's in use.

## OK, how do I make it go?

To run the proxy you'll need Java 8, a copy of the proxy, and a copy of the game. Download barchomat 
[here](barchomat.jar). The proxy reads game logic (hit points, dps etc for the various characters and buildings) from 
the android version of executable, which you'll need to find yourself (Google "clash of clans apk"). Put the proxy and 
the game executable in the same directory and start the proxy from the command line:

    java -jar barchomat.jar
    
The proxy will log a startup message if all is well:
    
    10:06:59 INFO  Reading protocol definition from Protocol.json
    10:07:00 DEBUG Reading logic from com.supercell.clashofclans-7.1.1-Android-4.0.3.apk
    10:07:00 INFO  Listening on 9339  

The second part is harder; you have to convince your device to talk to the proxy rather than the real Clash server. 
The easiest way of doing this is changing the DNS entries for `game.clashofclans.com` and `gamea.clansofclans.com` 
to point to the machine you're running the proxy on. If you're playing on a phone or tablet this will typically mean 
connecting through WiFi and changing the entries for the LAN.  

You probably won't want to redirect all Clash traffic on your LAN to the proxy—that would interfere with your day to 
day clashing. Instead, run a local DNS (I use [dnsmasq][1]) and set your device to use it. There are a few ways set 
your game up to use an alternate DNS:

 - Alter the DNS settings on the device directly
 - Play the game in an emulator on a machine set to use the alternate DNS
 - Share the connection of a machine set to use the alternate DNS with your device over WiFi 

Make sure that the firewall on the proxy machine allows connections to port 9339, then fire up your game. You should
see the connection logged in your console:

    10:39:07 INFO  Client connected from /192.168.1.2
    10:39:08 INFO  Sir Barchable
    10:39:08 INFO  DPS: 344, HP: 27065 (walls 138000)
    10:39:08 INFO  Garrison: [lvl 3 Healer x 1]
    10:39:08 INFO  Town Hall: Loot[g=1000, e=1000, de=0]
    10:39:08 INFO  Storage: Loot[g=70967, e=226135, de=0]
    10:39:08 INFO  Castle: Loot[g=0, e=0, de=0]
    10:39:08 INFO  Collectors: Loot[g=6976, e=8651, de=0]
    10:39:08 INFO  Total: Loot[g=78943, e=235786, de=0]
    
If you don't see any output from the proxy, and your game runs fine, it's likely that the device is not picking up the 
alternate DNS entries. If your game freezes or displays an error there's a problem with the connection between the 
device and the proxy.  

## This is crap, and I want to help make it better
  
If you know Java, fork the code and start hacking from the outside (ClashProxy.java), or the inside 
(VillageAnalyzer.java). If anything useful comes of it mail me at *sir.barchable@gmail.com*. 

If you're handy with stream capture tools like [tcpflow][2] you can help out with protocol analysis.
We've described the protocol packet structure in a collection of JSON files, so no programming is required.
 
[1]: http://www.thekelleys.org.uk/dnsmasq/doc.html
[2]: https://github.com/simsong/tcpflow
