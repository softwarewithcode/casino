# casino

## User interface 23/12/2022
Frontend folder contains UI which is required to build and run separately during development phase. Original tester.html is updated no longer. Intention is to bring some test versions of the UI at some point into web-module.
<br> Clicking "open table" initiates a socket session, browser tries to connect ws://localhost:8080/casino/blackjack/{tableId} 
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
