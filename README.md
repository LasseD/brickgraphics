# brickgraphics

Realizing the website that is hosting this project is not doing so well right now, this is how to install and run locally:

## Install JAVA SDK

Use https://openjdk.org/ or similar to get the command line tools "javac" and "java"

## Compile

javac -cp src -d bin src/mosaic/controllers/MainController.java

## Run

java -cp bin mosaic.controllers.MainController
