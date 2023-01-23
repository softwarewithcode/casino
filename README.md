# casino

## Docker
Dockerfile not yet fully working. If you have time and interest to get the Docker working -> pull request or a message would be cool for working setup.

## Workspace instructions from scratch<br>
/utils/workspace folder

## User interface 
Current UI is only a reference implementation without any fancy animations. <br> If you are interested in developing your own version of the UI then reference implementation and it's communication with backend might be a place to start. Frontend folder contains UI sources.

UI <br>
1)fetches tables from http://localhost:8080/casino/blackjack/tables <br>
2)connects to a selected table at ws://localhost:8080/casino/blackjack/{tableId} <br>
Expected context root for web-app is /casino which is possible to configure when starting backend server. <br>
For example if running TomEE plume, it can be configured to use 8080 port and "/casino" context path. <br>
->  server.xml -> <Context docBase="web" path="/casino" reloadable="true"  <br>


##
Backend portion contains the compiled UI files. So it's possible to check the app without setting up the UI environment. <br>
http://localhost:8080/casino/ with pre compiled files, without setting the frontend dev environment. <br>

## initial idea and goals 25/11/2022
* Build a casino where play money is used 
* Start with a blackjack game 
* Public and private tables with custom token coins
* Free daily play money reload if all is lost
* Username and guest players
* Once the user 'withdraws' the play money could be converted to top list position.
* Start development from database and the core game itself
* Build UI on top of working internals
* Partly reuse code from my previous projects
## Technology
* JakartaEE backend with MariaDB for the core data. Finished hands into MongoDB cloud?
* UI: Browser? Html canvas for visualization?
* Timetable?
* OpenJDK 19 with compiler level 19
* used IDE Eclipse 2022-09 -> import as Maven projects
* running tests in Eclipse -> run as JUnit test from the main test folder level "blackjack/src/test/java" with mouse right click
