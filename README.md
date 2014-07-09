docker-netarchivesuite
======================

Tool for generating Dockerfiles for running 
[NetarchiveSuite Quickstart](https://sbforge.org/display/NASDOC/Quick+Start+Manual) in different environment. The current 
environment variables are Linux distribution (Unbuntu 13.04/14.04, Centos6/7) and Java flavor Oracle-jdk6/jdk7, 
openjdk6/7.

The generated dockerfiles can then be used to build the relevant images.

## Requirements
Both [Docker](https://docs.docker.com/) and [Groovy](groovy.codehaus.org) needs to me install. 

Use Homebrew and Boot2Docker to setup on Mac.
  
## Generation Dockerfiles
 
Run the generatedockerfiles.groovy groovy script:    `
 
```
 groovy generatedockerfiles.groovy
```

See generatedockerfiles.groovy for detailed information on generation steps.

This will produce a dir *docker-files/$linuxdist-$javaflavor with contains the generates Dockerfile + additional 
resources needed to build the Docker image.

## Building images

Use the build script located in the relevante *docker-files* dir, eg.

```
cd docker-files/ubuntu-14.04-oracle-jdk6
./build.sh
```
The build image will be tagged as *netarchivesuite/$linuxdist-$javaflavor, eg. *netarchivesuite/ubuntu-14.04-oracle-jdk6*

See [Docker builder](http://docs.docker.com/reference/builder/) for further information.

## Running quickstart from the Docker images

Start the Quickstart based on a specific docker image with *docker run*:

  ```
  docker run -di -P --name quickstart netarchivesuite/ubuntu-14.04-oracle-jdk6 
  ```
  
See [docker run](http://docs.docker.com/reference/builder/#run/) for further information.
