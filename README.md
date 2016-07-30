# SomaScraper
A web-scraper for compiling playlist data from somafm built on javafx, sqlite, and jsoup.

I am in no way affiliated with SomaFM, this project is an independent work.  I do, however, enjoy what they do and encourage fans of good music to visit and/or support them at <a href="http://www.somafm.com/">http://www.somafm.com/</a>

## Build
Build requires <a href="http://gradle.org">gradle</a> and JDK8 or greater.

-navigate to /somascraper/
-gradle build

If you're building for Raspberry PI, or any other Arm based platform, there are some extra dependencies to get the project running.

-<a href="https://wiki.openjdk.java.net/display/OpenJFX/Main">OpenJFX</a>, as Oracle no longer includes it with ARM distributions.
-You have to build <a href="https://github.com/xerial/sqlite-jdbc">sqlite-jdbc</a> yourself from it's repository (3.13.0 as of 7/30/16) as the release version has improperly built binaries for the ARMHF architecture.

## Use 
Scraping is automated through windows' task scheduler and a .bat file.  You'll want to set this up yourself depending on your system.  I found running the scraper every 30 minutes is a good balance between aggressive and not missing songs as they're played.

The email component was to automate delivering the playlist to interested parties automatically.

The actual UI was layered on top as an exercise.