#!/bin/bash

mvn clean package
java -jar output/n-jar-with-dependencies.jar n.Runner