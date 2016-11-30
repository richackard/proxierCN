# proxierCN

THIS IS A PERSONAL PROJECT, JUST FOR FUN. 

This project has two parts. One server, one client.

Server is in development while client's dev has not yet started. The original motivation of this project is that there are some Chinese apps and sites have regional restrictions. Although I can set proxy server of China easily manually (first search and then set), it is a repeated boring task and I don't want to do that (PROGRAMMERS ARE LAZY). Therefore, I wanted to develop such a server-client thing that can help me to automatically set proxy server by a "single-click" on my devices.

The proxierCN server's functionality is quite simple, it continuously fetching proxy servers online and analyze the usability of those servers and it also maintains a small db which contains all the available proxy servers it has fetched. It will delete proxy servers that it thinks no long work according to certain rules.
